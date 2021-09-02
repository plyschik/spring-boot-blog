import React from 'react';
import ReactDOM from 'react-dom';
import { InternationalizationProvider } from './contexts/InternationalizationContext';
import { CommentsProvider } from './contexts/CommentsContext';
import CommentsComponent from './components/CommentsComponent';

const container = document.querySelector('#sbb-comments');
const i18n = JSON.parse(container.dataset.i18n);
const postId = parseInt(container.dataset.postId, 10);
const isAnonymous = container.dataset.isAnonymous === 'true';

ReactDOM.render(
  <InternationalizationProvider i18n={i18n}>
    <CommentsProvider postId={postId}>
      <CommentsComponent isAnonymous={isAnonymous} postId={postId} />
    </CommentsProvider>
  </InternationalizationProvider>,
  container
);
