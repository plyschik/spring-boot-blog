import React from 'react';
import PropTypes from 'prop-types';
import CommentsList from './CommentsList';
import CommentCreate from './CommentCreate';

const CommentsComponent = ({ i18n, postId }) => (
  <>
    <CommentCreate postId={postId} />
    <CommentsList i18n={i18n} postId={postId} />
  </>
);

CommentsComponent.propTypes = {
  i18n: PropTypes.objectOf(PropTypes.string).isRequired,
  postId: PropTypes.string.isRequired,
};

export default CommentsComponent;
