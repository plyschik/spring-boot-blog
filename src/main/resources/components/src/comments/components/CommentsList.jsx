import React, { useContext, useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { InternationalizationContext } from '../contexts/InternationalizationContext';
import { CommentsContext } from '../contexts/CommentsContext';
import Comment from './Comment';
import CommentEditModal from './CommentEditModal';
import PaginationComponent from './PaginationComponent';

const CommentsList = ({ postId }) => {
  const i18n = useContext(InternationalizationContext);
  const {
    state: commentsState,
    fetchFirstPage,
    updateComments,
  } = useContext(CommentsContext);
  const [commentToEdit, setCommentToEdit] = useState(null);

  const handleEditComment = (post, id, content) => {
    setCommentToEdit({
      post,
      id,
      content,
    });
  };

  const handleEditUpdate = (comment) => {
    updateComments(comment);
  };

  const handleCloseModal = () => {
    setCommentToEdit(null);
  };

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
          handleEditComment={handleEditComment}
        />
      ))}
      {commentToEdit && (
        <CommentEditModal
          comment={commentToEdit}
          handleEditUpdate={handleEditUpdate}
          handleCloseModal={handleCloseModal}
        />
      )}
      <PaginationComponent postId={postId} />
    </>
  );
};

CommentsList.propTypes = {
  postId: PropTypes.number.isRequired,
};

export default CommentsList;
