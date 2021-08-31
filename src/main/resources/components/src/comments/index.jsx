import React from 'react';
import ReactDOM from 'react-dom';
import { InternationalizationProvider } from './contexts/InternationalizationContext';
import { PaginationProvider } from './contexts/PaginationContext';
import { CommentsProvider } from './contexts/CommentsContext';
import CommentsComponent from './components/CommentsComponent';

const container = document.querySelector('#sbb-comments');

ReactDOM.render(
  <InternationalizationProvider i18n={JSON.parse(container.dataset.i18n)}>
    <PaginationProvider>
      <CommentsProvider postId={container.dataset.postId}>
        <CommentsComponent
          isAnonymous={container.dataset.isAnonymous === 'true'}
          postId={container.dataset.postId}
        />
      </CommentsProvider>
    </PaginationProvider>
  </InternationalizationProvider>,
  container
);
