import * as React from 'react';
import classnames from 'classnames';
import NavigationView from '~/components/layout/NavigationView';
import styles from './index.css';
import { gameFetch } from '~/state/actions';
import { useSelector, useDispatch } from 'react-redux';
import Tile from './Tile';

const Board = (props) => {
  const { currentPlayerId, tiles } = props;
  const maxRow = tiles.reduce((z, t) => Math.max(t.point.i, z), 0);
  const size = 'calc(' + (100 / (maxRow + 1)) + 'vmin - 16px)';
  const grid = [...Array(maxRow + 1).keys()]
    .map(i => tiles.filter(_ => _.point.i === i))
    .map(_ => _.sort((a, b) => a.point.j - b.point.j));

  return (
    <div>{grid.map((row, i) =>
      <div key={i} className={styles.boardRow}>{row.map((tile, j) =>
        <Tile
          key={j}
          currentPlayerId={currentPlayerId}
          tile={tile}
          size={size}
        />
      )}</div>
    )}</div>
  );
}

const GameView = (props) => {
  const { gameId } = props.match.params;
  const dispatch = useDispatch();
  const game = useSelector(_ => _.games.game);

  // componentDidMount
  React.useEffect(() => {
    dispatch(gameFetch(gameId));
  }, []);

  return (
    <>
      <NavigationView />
      <div className={styles.root}>
        {game ?
          <Board
            tiles={game.state.tiles}
            currentPlayerId={game.state.currentPlayerId}
          /> : null
        }
      </div>
    </>
  );
};

export default GameView;