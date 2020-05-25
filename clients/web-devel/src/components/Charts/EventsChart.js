import React, { Component } from 'react';
import {
  PieChart, Pie, Tooltip
} from 'recharts';
import {useSelector} from "react-redux";

const formatData = (inputData) => {
  return inputData.map((element, idx) => {
    return {
      name: element.domain,
      value: element.duration,
      rank: idx + 1
    };
  });
};

const getTooltipAnchor = (sectorData) => {
  if (sectorData.y >= sectorData.cy * 1.5 || sectorData.y <= sectorData.cy * 0.5 ) {
    return "middle";
  } else if (sectorData.x > sectorData.cx) {
    return "start";
  } else {
    return "end";
  }
};

function renderPieLabel(sectorData) {
  return <text x={sectorData.x} y={sectorData.y} fill="#666" textAnchor={getTooltipAnchor(sectorData)}>{`#${sectorData.payload.rank}: ${sectorData.name} (${Math.round(sectorData.percent * 100)}%)`}</text>;
}
const EventsChart = (props) => {
  const data = useSelector(_ => _.events.topEvents);
  return (
    <PieChart width={800} height={800}>
      <Pie
        dataKey="value"
        data={formatData(data)}
        isAnimationActive={false}
        cx={400}
        cy={400}
        outerRadius={160}
        fill="#8884d8"
        label={renderPieLabel}
      />
      <Tooltip />
    </PieChart>
  );
};

export default EventsChart;