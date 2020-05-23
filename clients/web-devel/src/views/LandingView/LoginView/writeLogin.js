import fetch from '~/api/fetch';
import gql from 'graphql-tag';

export default function writeAuth(q) {
  return fetch(query)({ q });
}

const query = gql`
  query login($q: CredentialsQuery!) {
    login(q: $q) {
      userId
      bearerToken
      username
      success
      message
    }
  }
`;
