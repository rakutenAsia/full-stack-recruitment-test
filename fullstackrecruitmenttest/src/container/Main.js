import React, { Component } from 'react';
import axios from 'axios';

class Main extends Component {

  ping() {
    axios.get("http://localhost:8080/test").then(res => {
    alert("Received Successful response from server!" + res);
  }, err => {
    alert("Server rejected response with: " + err);
  });
  }

  render() {
    return (
      <div className="Main">
          <h1>Welcome to React</h1>
            <div>
              <button onClick={this.ping}>Ping!</button>
            </div>
      </div>
    );
  }
}

export default Main;
