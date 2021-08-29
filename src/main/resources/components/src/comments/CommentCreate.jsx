import React, { useContext, useState } from 'react';
import PropTypes from 'prop-types';
import axios from 'axios';
import classNames from 'classnames';
import { CommentsContext } from './CommentsContext';

const CommentCreate = ({ postId }) => {
  const [, dispatch] = useContext(CommentsContext);

  const [loading, setLoading] = useState(false);
  const [content, setContent] = useState('');
  const [contentError, setContentError] = useState(null);

  const handleContentChange = (event) => {
    setContent(event.target.value);
  };

  const handleSubmit = (event) => {
    event.preventDefault();

    setContentError(null);
    setLoading(true);

    axios.post(`/api/posts/${postId}/comments`, { content })
      .then((response) => {
        setContent('');
        dispatch({
          type: 'append',
          payload: response.data,
        });
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
            placeholder="Comment"
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
        Create
      </button>
    </form>
  );
};

CommentCreate.propTypes = {
  postId: PropTypes.string.isRequired,
};

export default CommentCreate;
