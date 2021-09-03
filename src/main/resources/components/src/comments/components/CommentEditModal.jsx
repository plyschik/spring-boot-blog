import React, { useContext, useState } from 'react';
import PropTypes from 'prop-types';
import axios from 'axios';
import Modal from 'react-bootstrap/Modal';
import Form from 'react-bootstrap/Form';
import Button from 'react-bootstrap/Button';
import { InternationalizationContext } from '../contexts/InternationalizationContext';

const CommentEditModal = ({ comment, handleEditUpdate, handleCloseModal }) => {
  const i18n = useContext(InternationalizationContext);

  const [loading, setLoading] = useState(false);
  const [content, setContent] = useState(comment.content);
  const [contentError, setContentError] = useState(null);

  const handleClose = () => {
    setLoading(false);
    setContent('');
    setContentError(null);
    handleCloseModal();
  };

  const handleContentChange = (event) => {
    if (contentError) {
      setContentError(null);
    }

    setContent(event.target.value);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    setContentError(null);
    setLoading(true);

    try {
      const response = await axios.patch(
        `/api/posts/${comment.post}/comments/${comment.id}`,
        {
          content,
        }
      );
      handleEditUpdate(response.data);
      handleClose();
    } catch (error) {
      if (error.response) {
        if (error.response.status === 400) {
          setContentError(error.response.data.errors[0].message);
        }
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal show backdrop="static" keyboard={false}>
      <Modal.Header closeButton onHide={handleClose}>
        <Modal.Title>{i18n.comment_edit}</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Form method="POST" onSubmit={handleSubmit}>
          <Form.Group className="mb-3">
            <Form.Control
              as="textarea"
              rows={3}
              isInvalid={contentError}
              disabled={loading}
              value={content}
              onChange={handleContentChange}
              placeholder={`${i18n.comment}...`}
            />
            <Form.Control.Feedback type="invalid">
              {contentError}
            </Form.Control.Feedback>
          </Form.Group>
          <div className="d-flex justify-content-end">
            <Button type="submit" disabled={loading}>
              {loading && (
                <span className="me-2 spinner-border spinner-border-sm" />
              )}
              {i18n.update}
            </Button>
          </div>
        </Form>
      </Modal.Body>
    </Modal>
  );
};

CommentEditModal.propTypes = {
  comment: PropTypes.shape({
    post: PropTypes.number.isRequired,
    id: PropTypes.number.isRequired,
    content: PropTypes.string.isRequired,
  }).isRequired,
  handleEditUpdate: PropTypes.func.isRequired,
  handleCloseModal: PropTypes.func.isRequired,
};

export default CommentEditModal;
