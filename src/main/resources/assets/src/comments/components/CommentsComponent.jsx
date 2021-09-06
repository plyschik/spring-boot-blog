import React, { useContext } from 'react';
import PropTypes from 'prop-types';
import { InternationalizationContext } from '../contexts/InternationalizationContext';
import CommentCreate from './CommentCreate';
import CommentsList from './CommentsList';
import { CommentsContext } from '../contexts/CommentsContext';

const CommentsComponent = ({ isAnonymous, postId }) => {
  const i18n = useContext(InternationalizationContext);
  const { state: commentsState } = useContext(CommentsContext);

  return (
    <>
      <h3 className="my-4">{`${i18n.comments} (${commentsState.pagination.totalElements})`}</h3>
      <CommentCreate isAnonymous={isAnonymous} postId={postId} />
      <CommentsList postId={postId} />
    </>
  );
};

CommentsComponent.propTypes = {
  isAnonymous: PropTypes.bool.isRequired,
  postId: PropTypes.number.isRequired,
};

export default CommentsComponent;
