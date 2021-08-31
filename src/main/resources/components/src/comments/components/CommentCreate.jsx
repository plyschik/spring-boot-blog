import React, { useContext, useState } from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import axios from 'axios';
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
    <form
      className="mb-3"
      method="POST"
      onSubmit={handleSubmit}
    >
      <div className="mb-3">
        <div className="input-group">
          <textarea
            className={classNames('form-control', { 'is-invalid': contentError })}
            rows="3"
            placeholder={i18n.comment}
            value={content}
            disabled={loading}
            onChange={handleContentChange}
          />
          {contentError && <div className="invalid-feedback">{contentError}</div>}
        </div>
      </div>
      <button
        className="btn btn-primary"
        type="submit"
        disabled={loading}
      >
        {loading && <span className="me-2 spinner-border spinner-border-sm" />}
        {i18n.create}
      </button>
    </form>
  );
};

CommentCreate.propTypes = {
  isAnonymous: PropTypes.bool.isRequired,
  postId: PropTypes.string.isRequired,
};

export default CommentCreate;
