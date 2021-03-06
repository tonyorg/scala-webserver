import fetch from '~/api/fetch';
import gql from 'graphql-tag';

export default function fetchEvents(q) {
  return fetch(query)({ q });
}

const query = gql`
  query getEvents($q: TopDomainsQuery!) {
    fetchTopDomains(q: $q) {
      success
      message
      events{
        domain
        duration
      }
    }
  }
`;
