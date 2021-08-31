import React, { useContext, useState } from 'react';
import PropTypes from 'prop-types';
import axios from 'axios';
import Form from 'react-bootstrap/Form';
import Button from 'react-bootstrap/Button';
import { InternationalizationContext } from '../contexts/InternationalizationContext';
import { CommentsContext } from '../contexts/CommentsContext';

const CommentCreate = ({ isAnonymous, postId }) => {
  const i18n = useContext(InternationalizationContext);
  const { fetchFirstPage } = useContext(CommentsContext);

  const [loading, setLoading] = useState(false);
  const [content, setContent] = useState('');
  const [contentError, setContentError] = useState(null);

  const handleContentChange = (event) => {
    if (contentError) {
      setContentError(null);
    }

    setContent(event.target.value);
  };

  const handleSubmit = (event) => {
    event.preventDefault();

    setContentError(null);
    setLoading(true);

    axios.post(`/api/posts/${postId}/comments`, { content })
      .then(() => {
        setContent('');
        fetchFirstPage();
      })
      .catch((error) => {
        if (error.response) {
          if (error.response.status === 400) {
            setContentError(error.response.data.errors[0].message);
          }
        }
      })
      .finally(() => {
        setLoading(false);
      });
  };

  if (isAnonymous) {
    return (
      <div className="alert alert-info">{i18n.only_authenticated_users_can_create_comments}</div>
    );
  }

  return (
    <Form className="mb-3" method="POST" onSubmit={handleSubmit}>
      <Form.Group className="mb-3">
        <Form.Label>{i18n.comment}</Form.Label>
        <Form.Control
          as="textarea"
          rows={3}
          isInvalid={contentError}
          disabled={loading}
          value={content}
          onChange={handleContentChange}
        />
        <Form.Control.Feedback type="invalid">{contentError}</Form.Control.Feedback>
      </Form.Group>
      <Button type="submit" disabled={loading}>
        {loading && <span className="me-2 spinner-border spinner-border-sm" />}
        {i18n.create}
      </Button>
    </Form>
  );
};

CommentCreate.propTypes = {
  isAnonymous: PropTypes.bool.isRequired,
  postId: PropTypes.string.isRequired,
};

export default CommentCreate;
