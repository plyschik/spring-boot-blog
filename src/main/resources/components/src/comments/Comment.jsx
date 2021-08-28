import React from 'react';
import PropTypes from 'prop-types';

const Comment = ({
  i18n,
  postId,
  id,
  content,
  createdAt,
  fullName,
  canEdit,
  canDelete,
}) => (
  <div className="mb-3 card">
    <div className="card-body">
      <div className="mb-2 card-text">{content}</div>
      <small className="text-muted">
        <span>{fullName}</span>
        <span className="mx-1">&bull;</span>
        <span>{new Date(createdAt).toLocaleString()}</span>
      </small>
      {(canEdit || canDelete) && (
        <div className="mt-2">
          {canEdit && <a className="me-1 btn btn-sm btn-success" href={`/posts/${postId}/comments/${id}/edit`}>{i18n.edit}</a>}
          {canDelete && <a className="btn btn-sm btn-danger" href={`/posts/${postId}/comments/${id}/delete`}>{i18n.delete}</a>}
        </div>
      )}
    </div>
  </div>
);

Comment.propTypes = {
  i18n: PropTypes.objectOf(PropTypes.string).isRequired,
  postId: PropTypes.string.isRequired,
  id: PropTypes.number.isRequired,
  content: PropTypes.string.isRequired,
  createdAt: PropTypes.string.isRequired,
  fullName: PropTypes.string.isRequired,
  canEdit: PropTypes.bool.isRequired,
  canDelete: PropTypes.bool.isRequired,
};

export default Comment;
