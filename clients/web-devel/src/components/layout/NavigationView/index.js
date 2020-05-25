import * as React from 'react';
import ConnectionView from './ConnectionView';
import Nav from 'react-bootstrap/Nav';
import Navbar from 'react-bootstrap/Navbar';
import { Link } from 'react-router-dom';
import {useDispatch} from "react-redux";
import { authLogout } from '~/state/actions';

const NavigationView = (props) => {
  const dispatch = useDispatch();
  const onLogout = () => dispatch(authLogout());
  return (
    <Navbar bg='light' variant='light'>
      <Navbar.Brand href='/'>ProductionTracker</Navbar.Brand>
      <Navbar.Collapse>
        <Nav className='mr-auto'>
          <Link to='/' onClick={onLogout}>Logout</Link>
        </Nav>
        <ConnectionView />
      </Navbar.Collapse>
    </Navbar>
  );
};

export default NavigationView;
