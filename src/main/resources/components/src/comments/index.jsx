import React from 'react';
import ReactDOM from 'react-dom';
import CommentsList from './CommentsList';

const container = document.querySelector('#sbb-comments');

ReactDOM.render(
  <CommentsList i18n={JSON.parse(container.dataset.i18n)} postId={container.dataset.postId} />,
  container,
);
