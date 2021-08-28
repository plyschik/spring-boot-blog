const path = require('path');

module.exports = {
  mode: 'development',
  entry: {
    comments: './src/comments',
  },
  output: {
    path: path.resolve(__dirname, '../static/assets/js'),
    filename: '[name].bundle.js',
  },
  module: {
    rules: [
      {
        test: /\.jsx?$/i,
        exclude: /node_modules/,
        use: {
          loader: 'babel-loader',
          options: {
            presets: [
              '@babel/preset-env',
              '@babel/preset-react',
            ],
          },
        },
      },
    ],
  },
  resolve: {
    extensions: ['.js', '.jsx'],
  },
};
