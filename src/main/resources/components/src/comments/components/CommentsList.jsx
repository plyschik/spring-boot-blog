import React, { useContext, useEffect } from 'react';
import PropTypes from 'prop-types';
import { InternationalizationContext } from '../contexts/InternationalizationContext';
import { CommentsContext } from '../contexts/CommentsContext';
import Comment from './Comment';
import PaginationComponent from './PaginationComponent';

const CommentsList = ({ postId }) => {
  const i18n = useContext(InternationalizationContext);
  const { state: commentsState, fetchFirstPage } = useContext(CommentsContext);

  useEffect(() => {
    fetchFirstPage();
  }, []);

  if (commentsState.loading) {
    return (
      <div className="p-5 d-flex justify-content-center align-items-center">
        <div className="me-3 spinner-grow" />
        <strong>{i18n.loading}</strong>
      </div>
    );
  }

  if (commentsState.comments.length === 0) {
    return <div>{i18n.empty_list}</div>;
  }

  return (
    <>
      {commentsState.comments.map((comment) => (
        <Comment
          key={comment.id}
          postId={postId}
          id={comment.id}
          content={comment.content}
          createdAt={comment.createdAt}
          fullName={`${comment.user.firstName} ${comment.user.lastName}`}
          canEdit={comment.canEdit}
          canDelete={comment.canDelete}
        />
      ))}
      <PaginationComponent postId={postId} />
    </>
  );
};

CommentsList.propTypes = {
  postId: PropTypes.number.isRequired,
};

export default CommentsList;
