import * as React from 'react';
import Jumbotron from 'react-bootstrap/Jumbotron';
import Form from 'react-bootstrap/Form';
import styles from './index.css';
import writeAuth from './writeAuth';
import writeLogin from './writeLogin';
import Button from "react-bootstrap/Button";

const LoginView = (props) => {
  class LoginForm extends React.Component {
    constructor(props) {
      super(props);
      this.state = {
        username: "",
        password: ""
      }
      this.handleChange = this.handleChange.bind(this);
      this.handleSubmit = this.handleSubmit.bind(this);
    }

    handleChange(event) {
      const name = event.target.name;
      const value = event.target.value;
      this.state[name] = value;
    }

    handleSubmit(event) {
        event.preventDefault();
        const username = this.state.username;
        const password = this.state.password;
        writeLogin({
          username,
          password
        }).then(response => console.log(response));
        // }).then(r => props.onLogin(r.data.login));
    }

    render() {
      return (
        <Form onSubmit={this.handleSubmit}>
          <Form.Group>
            <Form.Label>Username</Form.Label>
            <Form.Control name="username" type = "text" placeholder="Enter username" onChange={this.handleChange}/>
          </Form.Group>
          <Form.Group>
            <Form.Label>Password</Form.Label>
            <Form.Control name="password" type = "password" placeholder="Enter password"  onChange={this.handleChange}/>
          </Form.Group>
          <Button variant="primary" type="submit">
            Login
          </Button>
        </Form>
      );
    }
  }

  return (
    <>
      <Jumbotron>
        <h1>Welcome to Productivity Tracker!</h1>
        <p>
          Please login to view and analyse your data.
        </p>
      </Jumbotron>
      <div className={styles.login}>
        <LoginForm/>
      </div>
    </>
  );
}

export default LoginView;
