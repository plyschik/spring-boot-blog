import React, { useContext, useEffect } from 'react';
import PropTypes from 'prop-types';
import { InternationalizationContext } from '../contexts/InternationalizationContext';
import { CommentsContext } from '../contexts/CommentsContext';
import Comment from './Comment';

const CommentsList = ({ postId }) => {
  const i18n = useContext(InternationalizationContext);
  const { loading, comments, fetchFirstPage } = useContext(CommentsContext);

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
      {!comments.length ? (
        <div>{i18n.empty_list}</div>
      ) : comments.map((comment) => (
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
    </>
  );
};

CommentsList.propTypes = {
  postId: PropTypes.string.isRequired,
};

export default CommentsList;
