import React, { useContext, useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { InternationalizationContext } from '../contexts/InternationalizationContext';
import { CommentsContext } from '../contexts/CommentsContext';
import Comment from './Comment';
import CommentEditModal from './CommentEditModal';
import CommentDeleteModal from './CommentDeleteModal';
import PaginationComponent from './PaginationComponent';

const CommentsList = ({ postId }) => {
  const i18n = useContext(InternationalizationContext);
  const {
    state: commentsState,
    fetchFirstPage,
    updateComment,
    deleteComment,
  } = useContext(CommentsContext);
  const [commentToEdit, setCommentToEdit] = useState(null);
  const [commentToDelete, setCommentToDelete] = useState(null);

  const setEditedComment = (post, id, content) => {
    setCommentToEdit({
      post,
      id,
      content,
    });
  };

  const handleCommentUpdate = (comment) => {
    updateComment(comment);
  };

  const handleCommentEditModalClose = () => {
    setCommentToEdit(null);
  };

  const setDeletedComment = (post, id) => {
    setCommentToDelete({
      post,
      id,
    });
  };

  const handleCommentDelete = (id) => {
    deleteComment(id);
  };

  const handleCommentDeleteModalClose = () => {
    setCommentToDelete(null);
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
          setEditedComment={setEditedComment}
          setDeletedComment={setDeletedComment}
        />
      ))}
      {commentToEdit && (
        <CommentEditModal
          comment={commentToEdit}
          handleCommentUpdate={handleCommentUpdate}
          handleCommentEditModalClose={handleCommentEditModalClose}
        />
      )}
      {commentToDelete && (
        <CommentDeleteModal
          comment={commentToDelete}
          handleCommentDelete={handleCommentDelete}
          handleCommentDeleteModalClose={handleCommentDeleteModalClose}
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
