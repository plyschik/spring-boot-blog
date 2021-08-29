import React from 'react';
import ReactDOM from 'react-dom';
import CommentsComponent from './CommentsComponent';
import { CommentsProvider } from './CommentsContext';

const container = document.querySelector('#sbb-comments');

ReactDOM.render(
  <CommentsProvider>
    <CommentsComponent
      i18n={JSON.parse(container.dataset.i18n)}
      postId={container.dataset.postId}
    />
  </CommentsProvider>,
  container,
);
