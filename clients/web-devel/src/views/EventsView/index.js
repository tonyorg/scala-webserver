import * as React from 'react';
import fetchEvents from './fetchEvents';
import { useSelector, useDispatch } from 'react-redux';
import { withRouter } from 'react-router-dom'
import { updateEventsDisplay } from '~/state/actions';
import fetchGames from "../GamesView/fetchGames";
import styles from "../DashboardView/index.css";
import Alert from "react-bootstrap/Alert";

import {scaleOrdinal} from '@vx/scale';
import {LegendOrdinal} from '@vx/legend';
import { RadialChart, ArcSeries, ArcLabel } from '@data-ui/radial-chart';
import { color as colors } from '@data-ui/theme';

const colorScale = scaleOrdinal({ range: colors.categories });
const data = [{ label: 'a', value: 200 }, { label: 'c', value: 150 }, { label: 'c', value: 21 }];

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

  return (
    <div className={styles.root}>

    <div style={{ display: 'flex', alignItems: 'center' }}>
      <RadialChart
        ariaLabel="This is a radial-chart chart of..."
        width={500}
        height={500}
        renderTooltip={({ event, datum, data, fraction }) => (
          <div>
            <strong>{datum.label}</strong>
            {datum.value} ({(fraction * 100).toFixed(2)}%)
          </div>
        )}
      >
        <ArcSeries
          data={data}
          pieValue={d => d.value}
          fill={arc => colorScale(arc.data.label)}
          stroke="#fff"
          strokeWidth={1}
          // label{arc => `${(arc.data.value).toFixed(1)}%`}
          labelComponent={<ArcLabel />}
          innerRadius={radius => 0.35 * radius}
          outerRadius={radius => 0.6 * radius}
          labelRadius={radius => 0.75 * radius}
        />
      </RadialChart>
      <LegendOrdinal
        direction="column"
        scale={colorScale}
        shape="rect"
        fill={({ datum }) => colorScale(datum)}
        labelFormat={label => label}
      />
    </div>
    </div>
  );
};

export default EventsView;