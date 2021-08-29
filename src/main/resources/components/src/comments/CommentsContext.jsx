import React, { createContext, useReducer } from 'react';
import PropTypes from 'prop-types';

const CommentsContext = createContext();

const reducer = (state, action) => {
  switch (action.type) {
    case 'set':
      return {
        comments: action.payload,
      };
    case 'append':
      return {
        comments: [
          action.payload,
          ...state.comments,
        ],
      };
    default:
      return state;
  }
};

const CommentsProvider = ({ children }) => {
  const [state, dispatch] = useReducer(reducer, {
    comments: [],
  });

  return (
    <CommentsContext.Provider value={[state, dispatch]}>
      {children}
    </CommentsContext.Provider>
  );
};

CommentsProvider.propTypes = {
  children: PropTypes.node.isRequired,
};

export {
  CommentsContext,
  CommentsProvider,
};
