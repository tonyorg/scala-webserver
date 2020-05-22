import * as React from 'react';
import rd3 from 'react-d3-library'
import node from './Charts/eventsD3'
import fetchEvents from './fetchEvents';
import { useSelector, useDispatch } from 'react-redux';
import { withRouter } from 'react-router-dom'
import { updateEventsDisplay } from '~/state/actions';
import fetchGames from "../GamesView/fetchGames";
import styles from "../DashboardView/index.css";
import Alert from "react-bootstrap/Alert";

const EventsView = (props) => {
  const dispatch = useDispatch();
  const userId = useSelector(_ => _.auth.userId);
  const bearerToken = useSelector(_ => _.auth.bearerToken);
  // const active = recent.filter(_ => _.status === 'Started');
  // onComponentDidMount, load data
  React.useEffect(() => {
    fetchEvents({userId, bearerToken}).then(_ => {

      // dispatch(updateEventsDisplay(_.data))
    })
  }, []);

  const RD3Component = rd3.Component;

  class EventsChart extends React.Component {
    constructor(props) {
      super(props);
      this.state = {d3: ''}
    }
    componentDidMount() {
      console.log(node);
      this.setState({d3: node})
    }
    render() {
      return (
        <div>
          <RD3Component data={this.state.d3}/>
        </div>
      )
    }
  }


  return (
    <div className={styles.root}>
      <EventsChart/>
    </div>
  );
  // return active.length > 0 ? <GamesTable viewerId={userId} games={active} /> : null;
};

export default EventsView;