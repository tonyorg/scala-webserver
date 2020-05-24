import Cookies from 'js-cookie';

const BearerKey = 'X-ProdTracker-Bearer-Token';
const UserKey = 'X-ProdTracker-User-Id';
const UsernameKey = 'X-ProdTracker-Username';

const poll = () => {
  const userId = Cookies.get(UserKey);
  const bearerToken = Cookies.get(BearerKey);
  const username = Cookies.get(UsernameKey);
  return {
    loggedIn: Boolean(userId),
    userId,
    bearerToken,
    username
  }
};

const headers = () => {
  const { userId, bearerToken } = poll();
  return {
    [UserKey]: userId,
    [BearerKey]: bearerToken
  };
};

const apply = (auth) => {
  const { userId, bearerToken, username } = auth;
  userId && Cookies.set(UserKey, userId);
  bearerToken && Cookies.set(BearerKey, bearerToken);
  username && Cookies.set(UsernameKey, username);
};

export default {
  poll,
  headers,
  apply
};
