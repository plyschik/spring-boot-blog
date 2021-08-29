import React, { useState, useEffect, useContext } from 'react';
import PropTypes from 'prop-types';
import axios from 'axios';
import classNames from 'classnames';
import Comment from './Comment';
import { CommentsContext } from './CommentsContext';

const CommentsList = ({ i18n, postId }) => {
  const [state, dispatch] = useContext(CommentsContext);

  const [loading, setLoading] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [isFirstPageAvailable, setIsFirstPageAvailable] = useState(false);
  const [isPreviousPageAvailable, setIsPreviousPageAvailable] = useState(false);
  const [isNextPageAvailable, setIsNextPageAvailable] = useState(false);
  const [isLastPageAvailable, setIsLastPageAvailable] = useState(false);

  const fetchComments = (page) => {
    setLoading(true);

    axios.get(`/api/posts/${postId}/comments?page=${page}`)
      .then(({ data: { comments: items, pagination } }) => {
        dispatch({
          type: 'set',
          payload: items,
        });
        setCurrentPage(pagination.currentPage);
        setTotalPages(pagination.totalPages);
        setIsFirstPageAvailable(pagination.currentPage > 0);
        setIsPreviousPageAvailable(pagination.hasPreviousPage);
        setIsNextPageAvailable(pagination.hasNextPage);
        setIsLastPageAvailable(pagination.currentPage < (pagination.totalPages - 1));
      })
      .catch(() => {
        setTimeout(() => {
          fetchComments(page);
        }, 1000);
      })
      .finally(() => {
        setLoading(false);
      });
  };

  const fetchFirstPage = () => {
    fetchComments(0);
  };

  const fetchPreviousPage = () => {
    fetchComments(currentPage - 1);
  };

  const fetchNextPage = () => {
    fetchComments(currentPage + 1);
  };

  const fetchLastPage = () => {
    fetchComments(totalPages - 1);
  };

  useEffect(() => {
    fetchFirstPage();
  }, []);

  if (loading) {
    return (
      <div className="p-5 d-flex justify-content-center align-items-center">
        <div className="me-3 spinner-grow" />
        <strong>{i18n.loading}</strong>
      </div>
    );
  }

  return (
    <>
      <div>
        {!state.comments.length ? (
          <div className="alert alert-info">{i18n.empty}</div>
        ) : state.comments.map((comment) => (
          <Comment
            key={comment.id}
            i18n={i18n}
            postId={postId}
            id={comment.id}
            content={comment.content}
            createdAt={comment.createdAt}
            fullName={`${comment.user.firstName} ${comment.user.lastName}`}
            canEdit={comment.canEdit}
            canDelete={comment.canDelete}
          />
        ))}
      </div>
      {(totalPages > 1) && (
        <nav>
          <ul className="pagination justify-content-center">
            <li className={classNames('page-item', { disabled: !isFirstPageAvailable })}>
              <span className="page-link cursor-pointer" onClick={fetchFirstPage}>&laquo;</span>
            </li>
            <li className={classNames('page-item', { disabled: !isPreviousPageAvailable })}>
              <span className="page-link cursor-pointer" onClick={fetchPreviousPage}>&lsaquo;</span>
            </li>
            <li className="page-item border border-start-0 border-end-0 px-3 d-flex align-items-center">
              {`${i18n.page} ${currentPage + 1} ${i18n.of} ${totalPages}`}
            </li>
            <li className={classNames('page-item', { disabled: !isNextPageAvailable })}>
              <span className="page-link cursor-pointer" onClick={fetchNextPage}>&rsaquo;</span>
            </li>
            <li className={classNames('page-item', { disabled: !isLastPageAvailable })}>
              <span className="page-link cursor-pointer" onClick={fetchLastPage}>&raquo;</span>
            </li>
          </ul>
        </nav>
      )}
    </>
  );
};

CommentsList.propTypes = {
  i18n: PropTypes.objectOf(PropTypes.string).isRequired,
  postId: PropTypes.string.isRequired,
};

export default CommentsList;
