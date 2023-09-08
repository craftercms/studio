import React from 'react';
import logo from './logo.svg';
import './App.css';

function App() {
  return (
    <div className="App">
      <header className="App-header">
        <p>Welcome to Your React Application on CrafterCMS Project.</p>
        <img src={logo} className="App-logo" alt="logo" />
        <p>Edit <code>sources/packages/my-app/src/App.tsx</code> and save.</p>
        <a
          className="App-link"
          href="https://reactjs.org"
          target="_blank"
          rel="noopener noreferrer"
        >
          Learn React
        </a>
        <div>
        <div className="note">
          <p>
            This site blueprint is the equivalent of a Create React App. It's a blank slate React application which you can build your own digital experience.
          </p>
          <ul>
            <li>To create new content types use the Project Tools &gt; Content Types menu on the left Sidebar</li>
            <li>To update markup, edit this template by clicking on the Options menu on the top toolbar and select "Edit Template"</li>
            <li>To modify this app, refer to <code>sources/packages/my-app/README.md</code></li>
            <li>Crafter documentation can be found <a className="App-link" href="https://docs.craftercms.org" target="_blank" rel="noopener noreferrer">here</a></li>
            <li>CrafterCMS authoring and developer training is available. Please contact <a className="App-link" href="mailto:info@craftercms.com">info@craftercms.com</a></li>
          </ul>
        </div>
      </div>
      </header>
    </div>
  );
}

export default App;
