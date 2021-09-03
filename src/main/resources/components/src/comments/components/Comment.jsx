import React, { useContext } from 'react';
import PropTypes from 'prop-types';
import Card from 'react-bootstrap/Card';
import Button from 'react-bootstrap/Button';
import { InternationalizationContext } from '../contexts/InternationalizationContext';

const Comment = ({
  postId,
  id,
  content,
  createdAt,
  fullName,
  canEdit,
  canDelete,
  setEditedComment,
  setDeletedComment,
}) => {
  const i18n = useContext(InternationalizationContext);

  return (
    <Card className="mb-3">
      <Card.Body>
        <Card.Text className="mb-1">{content}</Card.Text>
        <small className="text-muted">
          <span>{fullName}</span>
          <span className="mx-1">&bull;</span>
          <span>{new Date(createdAt).toLocaleString()}</span>
        </small>
        {(canEdit || canDelete) && (
          <div className="mt-2">
            {canEdit && (
              <Button
                className="me-1"
                variant="success"
                size="sm"
                onClick={() => setEditedComment(postId, id, content)}
              >
                {i18n.edit}
              </Button>
            )}
            {canDelete && (
              <Button
                variant="danger"
                size="sm"
                onClick={() => setDeletedComment(postId, id)}
              >
                {i18n.delete}
              </Button>
            )}
          </div>
        )}
      </Card.Body>
    </Card>
  );
};

Comment.propTypes = {
  postId: PropTypes.number.isRequired,
  id: PropTypes.number.isRequired,
  content: PropTypes.string.isRequired,
  createdAt: PropTypes.string.isRequired,
  fullName: PropTypes.string.isRequired,
  canEdit: PropTypes.bool.isRequired,
  canDelete: PropTypes.bool.isRequired,
  setEditedComment: PropTypes.func.isRequired,
  setDeletedComment: PropTypes.func.isRequired,
};

export default Comment;
