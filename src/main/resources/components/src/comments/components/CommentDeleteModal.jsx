import React, { useContext, useState } from 'react';
import PropTypes from 'prop-types';
import axios from 'axios';
import Modal from 'react-bootstrap/Modal';
import Button from 'react-bootstrap/Button';
import { InternationalizationContext } from '../contexts/InternationalizationContext';

const CommentDeleteModal = ({
  comment,
  handleCommentDelete,
  handleCommentDeleteModalClose,
}) => {
  const i18n = useContext(InternationalizationContext);
  const [loading, setLoading] = useState(false);

  const handleClose = () => {
    setLoading(false);
    handleCommentDeleteModalClose();
  };

  const handleConfirm = async () => {
    setLoading(true);
    await axios.delete(`/api/posts/${comment.post}/comments/${comment.id}`);
    handleCommentDelete(comment.id);
    handleClose();
  };

  return (
    <Modal show backdrop="static" keyboard={false}>
      <Modal.Header closeButton={!loading} onHide={handleClose}>
        <Modal.Title>{i18n.confirmation}</Modal.Title>
      </Modal.Header>
      <Modal.Body>{i18n.delete_message}</Modal.Body>
      <Modal.Footer>
        {!loading && (
          <Button variant="primary" onClick={handleClose}>
            {i18n.cancel}
          </Button>
        )}
        <Button variant="danger" disabled={loading} onClick={handleConfirm}>
          {loading && (
            <span className="me-2 spinner-border spinner-border-sm" />
          )}
          {i18n.confirm}
        </Button>
      </Modal.Footer>
    </Modal>
  );
};

CommentDeleteModal.propTypes = {
  comment: PropTypes.shape({
    post: PropTypes.number.isRequired,
    id: PropTypes.number.isRequired,
  }).isRequired,
  handleCommentDelete: PropTypes.func.isRequired,
  handleCommentDeleteModalClose: PropTypes.func.isRequired,
};

export default CommentDeleteModal;
