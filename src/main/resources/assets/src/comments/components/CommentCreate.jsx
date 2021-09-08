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

  const handleSubmit = async (event) => {
    event.preventDefault();

    setContentError(null);
    setLoading(true);

    try {
      await axios.post(`/api/posts/${postId}/comments`, { content });
      setContent('');
      fetchFirstPage();
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

  if (isAnonymous) {
    return (
      <div className="alert alert-info">
        {i18n.only_authenticated_users_can_create_comments}
      </div>
    );
  }

  return (
    <Form className="mb-4" method="POST" onSubmit={handleSubmit}>
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
      <Button type="submit" disabled={loading}>
        {loading && <span className="me-2 spinner-border spinner-border-sm" />}
        {i18n.create}
      </Button>
    </Form>
  );
};

CommentCreate.propTypes = {
  isAnonymous: PropTypes.bool.isRequired,
  postId: PropTypes.number.isRequired,
};

export default CommentCreate;
