import * as React from 'react';
import Auth from '~/api/auth';
import DashboardView from '~/views/DashboardView';
import LoginView from './LoginView';
import { useDispatch, useSelector } from 'react-redux'
import { authLoginSuccess } from '~/state/actions';

const LandingView = (props) => {
  // State
  const dispatch = useDispatch();
  const onLoginSuccess = (response) => {
    dispatch(authLoginSuccess(response));
  };

  const DisplayedView = (props) => {
    if (props.isLoggedIn) {
      return <DashboardView/>;
    } else {
      return <LoginView onLoginSuccess={onLoginSuccess} />;
    }
  };

  return <DisplayedView isLoggedIn={useSelector(_ => _.auth).loggedIn} />;
};

export default LandingView;
