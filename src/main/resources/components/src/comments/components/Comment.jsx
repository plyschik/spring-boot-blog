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
                href={`/posts/${postId}/comments/${id}/edit`}
              >
                {i18n.edit}
              </Button>
            )}
            {canDelete && (
              <Button
                variant="danger"
                size="sm"
                href={`/posts/${postId}/comments/${id}/delete`}
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
  postId: PropTypes.string.isRequired,
  id: PropTypes.number.isRequired,
  content: PropTypes.string.isRequired,
  createdAt: PropTypes.string.isRequired,
  fullName: PropTypes.string.isRequired,
  canEdit: PropTypes.bool.isRequired,
  canDelete: PropTypes.bool.isRequired,
};

export default Comment;
