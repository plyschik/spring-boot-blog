import React from 'react';
import PropTypes from 'prop-types';
import CommentsList from './CommentsList';
import CommentCreate from './CommentCreate';
import Pagination from './Pagination';

const CommentsComponent = ({ postId }) => (
  <>
    <CommentCreate postId={postId} />
    <CommentsList postId={postId} />
    <Pagination />
  </>
);

CommentsComponent.propTypes = {
  postId: PropTypes.string.isRequired,
};

export default CommentsComponent;
