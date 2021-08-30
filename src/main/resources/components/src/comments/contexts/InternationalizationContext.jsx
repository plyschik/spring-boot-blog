import React, { createContext } from 'react';
import PropTypes from 'prop-types';

const InternationalizationContext = createContext();

const InternationalizationProvider = ({ children, i18n }) => (
  <InternationalizationContext.Provider value={i18n}>
    {children}
  </InternationalizationContext.Provider>
);

InternationalizationProvider.propTypes = {
  children: PropTypes.node.isRequired,
  i18n: PropTypes.objectOf(PropTypes.string).isRequired,
};

export {
  InternationalizationContext,
  InternationalizationProvider,
};
