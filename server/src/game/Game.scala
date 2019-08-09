package monarchy.game

import scala.util.Random

case class Game(
  rand: Random,
  players: Seq[Player],
  board: Board,
  turns: Seq[Turn]
) {
  def currentTurn: Turn = turns.head

  def currentPlayer: Player = {
    players(turns.size % players.size)
  }

  def currentSelection: Option[Vec] = {
    currentTurn.move orElse currentTurn.select
  }

  def currentTile: Option[Tile] = {
    currentSelection.flatMap(board.tile)
  }

  def currentPiece: Option[Piece] = {
    currentTile.flatMap(_.piece)
  }

  def selections: Deltas = {
    currentTurn.canSelect match {
      case false => Set.empty
      case true =>
        val pid = currentPlayer.id
        val tiles = board.tiles.filter(_.piece.exists(_.playerId == pid))
        tiles.map(_.point).toSet
    }
  }

  def movements: Deltas = {
    val moveSet = for {
      tile <- currentTile
      piece <- currentPiece
      if currentTurn.canMove
      if piece.canMove
    } yield {
      val points = piece.conf.movement(tile.point)
      Game.reachablePoints(board, piece, tile.point, points)
    }
    moveSet.getOrElse { Deltas.empty }
  }

  def directions: Deltas = Deltas.AdjecentDeltas

  def attacks: Set[Deltas] = {
    val attackSet = for {
      tile <- currentTile
      piece <- currentPiece
      if currentTurn.canAttack
      if piece.canAttack
    } yield piece.conf.attackPatterns.pointSets(tile.point)
    attackSet.getOrElse { Set.empty }
  }

  def tileSelect(pid: PlayerId, p: Vec): Change[Game] = {
    playerGuard(pid).flatMap(_ => turnGuard(TileSelect(p)))
  }

  def tileDeselect(pid: PlayerId): Change[Game] = {
    playerGuard(pid).flatMap(_ => turnGuard(TileDeselect))
  }

  def moveSelect(pid: PlayerId, p: Vec): Change[Game] = {
    for {
      _ <- playerGuard(pid)
      game <- turnGuard(MoveSelect(p))
      nextGame <- currentTile match {
        case None => Reject.PieceActionWithoutSelection
        case Some(tile) =>
          tile.piece match {
            case None => Reject.PieceActionWithoutSelection
            case Some(_) if !movements(p) => Reject.MoveIllegal
            case Some(piece) => Accept(game.copy(board = game.board.move(tile.point, p)))
          }
      }
    } yield nextGame
  }

  def directionSelect(pid: PlayerId, dir: Vec): Change[Game] = {
    for {
      _ <- playerGuard(pid)
      game <- turnGuard(DirSelect(dir))
      nextGame <- currentTile match {
        case None => Reject.PieceActionWithoutSelection
        case Some(tile) =>
          tile.piece match {
            case None => Reject.PieceActionWithoutSelection
            case Some(_) if !directions(dir) => Reject.DirIllegal
            case Some(piece) =>
              val nextPiece = PlacedPiece(tile.point, piece.copy(currentDirection = dir))
              val nextBoard = game.board.place(nextPiece)
              Accept(game.copy(board = nextBoard))
          }
      }
    } yield nextGame
  }

  def attackSelect(pid: PlayerId, points: Deltas): Change[Game] = {
    for {
      _ <- playerGuard(pid)
      game <- turnGuard(AttackSelect(points))
      nextGame <- currentTile match {
        case None => Reject.PieceActionWithoutSelection
        case Some(tile) =>
          tile.piece match {
            case None => Reject.PieceActionWithoutSelection
            case Some(_) if !attacks(points) => Reject.AttackIllegal
            case Some(piece) =>
              val pattern = PointPattern.infer(tile.point, points)
              val effects = piece.conf.effectArea(tile.point, pattern).toSeq.sorted
              val updates = effects.flatMap {
                // Damage another unit. Compute blocking, directionality, and damage.
                case Attack(pt, power) =>
                  game.board.tile(pt) match {
                    case None => Nil
                    case Some(Tile(_, None)) => Nil
                    case Some(Tile(_, Some(pieceN))) => Seq {
                      def damage = math.round(power * (1 - pieceN.conf.armor)).toInt
                      if (piece.conf.blockable) {
                        val blockingBase = pieceN.conf.blocking + pieceN.blockingAjustment
                        // Compute angle of vec from origin to target with way unit is facing.
                        val attackDir = pt - tile.point
                        val theta = attackDir angle pieceN.currentDirection
                        val (blockingProb, blockingDir) = if (theta <= math.Pi / 4) {
                          (0.0, pieceN.currentDirection * -1)
                        } else if (theta >= 3 * math.Pi / 4) {
                          (blockingBase, pieceN.currentDirection)
                        } else {
                          val dir = pieceN.currentDirection
                          val turnDir = if (attackDir.curl(dir) > 0) dir.perpendicular else dir.perpendicular * -1
                          (blockingBase / 2, turnDir)
                        }
                        val blockingOutcome = rand.nextDouble <= blockingProb
                        if (blockingOutcome) {
                          PlacedPiece(pt, pieceN.copy(
                            currentDirection = blockingDir,
                            blockingAjustment = pieceN.blockingAjustment - ((blockingBase/blockingProb) - blockingProb)
                          ))
                        } else {
                          PlacedPiece(pt, pieceN.copy(
                            blockingAjustment = pieceN.blockingAjustment + blockingBase,
                            currentHealth = pieceN.currentHealth - damage
                          ))
                        }
                      } else {
                        PlacedPiece(pt, pieceN.copy(currentHealth = pieceN.currentHealth - damage))
                      }
                    }
                  }
                // Adds a shrub unit for unoccupied tiles.
                case GrowPlant(pt) =>
                  if (Game.canOccupy(game.board, pt))
                    Seq(PlacedPiece(pt, PieceGenerator(Shrub, pid, currentPlayer.direction)))
                  else
                    Nil
                // Heals all units for the same player
                case HealAll(power) =>
                  game.board.pieces(piece.playerId).map {
                    case pp @ PlacedPiece(_, pieceN) =>
                      pp.copy(piece = pieceN.copy(currentHealth = pieceN.currentHealth + power))
                  }
              }
              val nextBoard = updates.foldLeft(game.board) { _ place _ }
              val nextGame = game.copy(board = nextBoard)
              Accept(nextGame)
          }
      }
    } yield nextGame
  }

  def commitTurn(pid: PlayerId): Change[Game] = {
    playerGuard(pid).map { _ =>
      val updatesForAll = board.pieces.map {
        case pp @ PlacedPiece(pt, piece) =>
          pp.copy(
            piece = piece.copy(
              currentWait = piece.currentWait - 1,
              blockingAjustment = piece.blockingAjustment * Game.BlockingAdjustmentDecay
            )
          )
      }
      val updateForPiece = for {
        tile <- currentTile
        piece <- tile.piece
      } yield {
        val totalWait = currentTurn.actions.collect {
          case MoveSelect(_) => math.floor(piece.conf.maxWait / 2).toInt
          case AttackSelect(_) => math.ceil(piece.conf.maxWait / 2).toInt
        }.sum
        PlacedPiece(tile.point, piece.copy(currentWait = totalWait))
      }
      val updates = updatesForAll ++ updateForPiece.toSeq
      val nextBoard = updates.foldLeft(board) { _ place _ }
      this.copy(board = nextBoard, turns = Turn() +: turns)
    }
  }

  def playerGuard(pid: PlayerId): Change[Unit] = {
    if (currentPlayer.id == pid) Accept.Unit else Reject.ChangeOutOfTurn
  }

  def turnGuard(act: TurnAction): Change[Game] = {
    currentTurn.act(act).map(t => this.copy(turns = t +: turns.tail))
  }
}

object Game {
  val BlockingAdjustmentDecay = 0.9

  def reachablePoints(b: Board, piece: Piece, p0: Vec, points: Deltas): Deltas = {
    def recurrence(start: Vec, visited: Deltas): Deltas = {
      val adj = Deltas.AdjecentDeltas.map(start + _)
      val valid = (adj -- visited) & points
      val accessible = valid.filter(canPassThrough(b, piece, _))
      val reached = visited ++ accessible
      reached ++ accessible.flatMap(recurrence(_, reached))
    }
    recurrence(p0, Deltas.empty).filter(canOccupy(b, _))
  }

  def canOccupy(b: Board, p: Vec): Boolean = {
    b.tile(p).exists(_.piece.isEmpty)
  }

  def canPassThrough(b: Board, piece: Piece, p: Vec): Boolean = {
    b.tile(p).exists { tile =>
      tile.piece.forall { occupant =>
        def canPass = piece.playerId == occupant.playerId && occupant.conf.movesAside
        piece.conf.teleports || canPass
      }
    }
  }
}