import React from 'react';
import ReactDOM from 'react-dom';
import { InternationalizationProvider } from './contexts/InternationalizationContext';
import { CommentsProvider } from './contexts/CommentsContext';
import { PaginationProvider } from './contexts/PaginationContext';
import CommentsComponent from './components/CommentsComponent';

const container = document.querySelector('#sbb-comments');

ReactDOM.render(
  <InternationalizationProvider i18n={JSON.parse(container.dataset.i18n)}>
    <PaginationProvider>
      <CommentsProvider postId={container.dataset.postId}>
        <CommentsComponent postId={container.dataset.postId} />
      </CommentsProvider>
    </PaginationProvider>
  </InternationalizationProvider>,
  container,
);
