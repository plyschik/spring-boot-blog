import React, { useContext } from 'react';
import PropTypes from 'prop-types';
import { InternationalizationContext } from '../contexts/InternationalizationContext';
import { PaginationContext } from '../contexts/PaginationContext';
import CommentCreate from './CommentCreate';
import CommentsList from './CommentsList';
import PaginationComponent from './PaginationComponent';

const CommentsComponent = ({ isAnonymous, postId }) => {
  const i18n = useContext(InternationalizationContext);
  const { totalElements } = useContext(PaginationContext);

  return (
    <>
      <h3 className="my-4">{`${i18n.comments} (${totalElements})`}</h3>
      <CommentCreate isAnonymous={isAnonymous} postId={postId} />
      <CommentsList postId={postId} />
      <PaginationComponent />
    </>
  );
};

CommentsComponent.propTypes = {
  isAnonymous: PropTypes.bool.isRequired,
  postId: PropTypes.string.isRequired,
};

export default CommentsComponent;
