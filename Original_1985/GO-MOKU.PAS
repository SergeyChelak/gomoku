{                             GO-MOKU 4.0
            Copyright (c) 1985, 87 by Borland International, Inc.

  This program plays a very old Japanese game called GO-MOKU,
  perhaps better known as  5-in-line.   The game is played on
  a board with 19 x 19 squares, and the object of the game is
  to get 5 stones in a row.

  System requirements:  IBM PC and true compatibles
                        TURBO PASCAL 4.0
                        DOS 2.0 or later
                        128 K-bytes system memory (minimum)

  List of include modules:
    GO-HELP.INC

  List of data files:
    GO-MOKU.HLP   - Help text
}
program Gomoku;
{$R-}


Uses
  Crt;

const
  N            =  19;                            { Size of the board }
  Esc          = #27;
  NullKey      = #0;
  CtrlC        =  #3;
  Return       = #13;
  Space        = #32;
  AttackFactor =   4;                 { Importance of attack (1..16) }
                    { Value of having 0, 1,2,3,4 or 5 pieces in line }
  Weight       : array[0..6] of integer = (0, 0, 4, 20, 100, 500, 0);
  NormalColor  : integer = White;
  BorderColor  : integer = Yellow;
  BoardColor   : integer = Cyan;
  HeadingColor : integer = Brown;

type
  TypeOfWin  = (Null, Horiz, DownLeft, DownRight, Vert);
  BoardType  = (Empty, Cross, Nought);        { Contents of a square }
  ColorType  = Cross..Nought;                      { The two players }
  IndexType  = 1..N;                            { Index to the board }
  NumberType = 0..5;                    { Number of pieces in a line }
  LineType   = array[ColorType] of NumberType;
                                   { Value of square for each player }
  ValueType  = array[ColorType] of integer;
  MaxString  = string[255];     { Used only as a procedure parameter }

var
  Board      : array[IndexType, IndexType] of BoardType; { The board }
  Player     : ColorType;            { The player whose move is next }
  TotalLines : integer;             { The number of empty lines left }
  GameWon    : boolean;          { Set if one of the players has won }
  FileRead   : boolean;        { Help file read? ... Help system ... }
                    { Number of pieces in each of all possible lines }
  Line       :  array[0..3, IndexType, IndexType] of LineType;
                              { Value of each square for each player }
  Value      : array[IndexType, IndexType] of ValueType;
  X, Y       : IndexType;                         { Move coordinates }
  Command    : char;                         { Command from keyboard }
  AutoPlay   : boolean;           { The program plays against itself }

procedure Abort;
{ Exit from the program }
begin
  TextColor(White);
  TextBackground(Black);
  LowVideo;
  Window(1, 1, 80, 25);
  GotoXY(1, 24);
  Halt;
end; { Abort }

procedure SetUpScreen;
{ Sets up the screen with an empty board }
type
  Str5=string[5];

procedure WriteBoard(N : integer; Top, Middle, Bottom : Str5);
{ Print the empty board and the border }
var
  I, J : IndexType;

procedure WriteLetters;
{ Write the letters }
var
  i : IndexType;
begin
  TextColor(BorderColor);
  Write('  ');
  for I := 1 to N do
    Write(Chr(Ord('A') + I - 1):2);
  WriteLn;
end; { WriteLetters }

procedure WriteBoardLine(J : integer; S : Str5);
{ Write one line of the board }
var
  i : IndexType;
begin
  TextColor(BorderColor);
  Write(J:2, ' ');
  TextColor(BoardColor);
  Write(s[1]);
  for i := 2 to N - 1 do
    Write(S[2], S[3]);
  Write(S[4], S[5]);
  TextColor(BorderColor);
  WriteLn(' ', J:2);
end; { WriteBoardLine }

begin { WriteBoard }
  GotoXY(1, 1);
  WriteLetters;
  WriteBoardLine(N, Top);
  for J := N - 1 downto 2 do
    WriteBoardLine(J, Middle);
  WriteBoardLine(1, Bottom);
  WriteLetters;
end; { WriteBoard }

begin { SetUpScreen }
  WriteBoard(N, 'ÚÄÂÄ¿',
                'ÃÄÅÄ´',
                'ÀÄÁÄÙ');
  TextColor(NormalColor);
end; { SetUpScreen }

procedure GotoSquare(X, Y : IndexType);
begin
  GotoXY(2 + X * 2, N + 2 - Y);
end; { GotoSquare }

procedure PrintMove(Piece : ColorType; X, Y : IndexType);
{ Prints a move }
const
  PieceChar  : array[ColorType] of char = ('X', '0');
  PieceColor : array[ColorType] of byte = (White, LightGreen);
begin
  TextColor(PieceColor[Piece]);
  GotoXY(49, 9);
  Write(PieceChar[Piece], Chr(Ord('A') + X - 1):2, Y);
  ClrEOL;
  GotoSquare(X, Y);
  Write(PieceChar[Piece]);
  GotoSquare(X, Y);
  TextColor(NormalColor);
end; { PrintMove }

procedure ClearMove;
{ Clears the line where a move is displayed }
begin
  GotoXY(49, 9);
  ClrEOL;
end; { ClearMove }

procedure PrintMsg(Str : MaxString);
{ Prints a message }
begin
  TextColor(NormalColor);
  GotoXY(1, 23);
  Write(Str);
end; { Print }

procedure ClearMsg;
{ Clears the message about the winner }
begin
  GotoXY(1,23);
  ClrEOL;
end; { ClearMsg }

procedure WriteHelp(S : MaxString; HiLen : byte);
{ Use one video background for HiLen bytes of
  string, use other for HiLen + 1 to Length(s) }
begin
  TextBackground(NormalColor);
  TextColor(Black);
  Write(Copy(S, 1, HiLen));
  TextBackground(Black);
  TextColor(NormalColor);
  Write(Copy(S, HiLen + 1, Length(s) - HiLen));
end; { WriteHelp }

{
  Please note that the help system is modular and may be easily
  removed or incorporated into other programs.

  To remove the help system:
    1.  Delete all lines with the comment ... Help system ...
    2.  Delete the line that includes the HELP.INC file

  To incorporate the help system:
    1.  Declare a global type:  MaxString = string[255]
    2.  Include all lines with the comment ... Help system ...
    3.  Include the HELP.INC file
}
{$I GO-HELP.INC           ... Help system ... }

procedure WriteCommand(S : MaxString);
{ Highlights the first letter of S }
begin
  TextColor(NormalColor);
  Write(S[1]);
  TextColor(NormalColor - 8);
  Write(Copy(S, 2, Length(s) - 1));
end; { WriteCommand }

procedure ResetGame(FirstGame : boolean);
{ Resets global variables to start a new game }
var
  I, J : IndexType;
  D    : 0..3;
  C    : ColorType;
begin
  SetUpScreen;
  if FirstGame then
  begin
    TextColor(HeadingColor);
    GotoXY(49, 1);
    Write('T U R B O - G O M O K U');
    GotoXY(49, 3);
    WriteCommand('Newgame ');
    WriteCommand('Quit ');
    WriteCommand('Auto ');
    WriteCommand('Play ');
    WriteCommand('Hint');
    GotoXY(49, 5);                                    { ... Help system ... }
    WriteHelp('?-for Help    ', 1);                   { ... Help system ... }
    FirstGame := false;
  end
  else
  begin
    ClearMsg;
    ClearMove;
  end;
  for I := 1 to N do
    for J := 1 to N do
    begin                          { Clear tables }
      Board[I, J] := Empty;
      for C := Cross to Nought do
      begin
        Value[I, J, C] := 0;
        for D := 0 to 3 do
          Line[D, I, J, C] := 0;
      end;
    end; { for }
  Player := Cross;               { Cross starts }
  TotalLines := 2 * 2 * (N * (N - 4) + (N - 4) * (N - 4)); { Total number }
  GameWon := false;                                        { of lines     }
end; { ResetGame }

function OpponentColor(Player : ColorType) : ColorType;
begin
  if Player = Cross then
    OpponentColor := Nought
  else
    OpponentColor := Cross;
end; { OpponentColor }

procedure BlinkWinner(Piece : ColorType;
                       X, Y : IndexType;
                WinningLine : TypeOfWin);
{ Prints the 5 winning stones in blinking color }
const
  PieceChar  : array[ColorType] of char = ('X', '0');
  PieceColor : array[ColorType] of byte = (White, LightGreen);

var
  XHold, YHold : integer; { Used to store the position of the winning move }
  Dx, Dy       : integer; { Change in X and Y }

procedure BlinkRow(X, Y, Dx, Dy : integer);
{ Blink the row of 5 stones }
var
  I : integer;
begin
  TextColor(PieceColor[Piece] + blink);
  for I := 1 to 5 do
  begin
    GotoSquare(X, Y);
    Write(PieceChar[Piece]);
    X := X - Dx;
    Y := Y - Dy;
  end;
end; { BlinkRow }

begin { BlinkRow }
  TextColor(PieceColor[Piece]);
  GotoXY(49, 9);
  Write(PieceChar[Piece],
        Chr(Ord('A') + X - 1):2, Y);          { display winning move }
  ClrEOL;
  XHold := X;                            { preserve winning position }
  YHold := Y;
  case WinningLine of
    Horiz : begin
              Dx := 1;
              Dy := 0;
            end;
    DownLeft : begin
                  Dx := 1;
                  Dy := 1;
                end;
    Vert : begin
             Dx := 0;
             Dy := 1;
           end;
    DownRight : begin
                  Dx := -1;
                  Dy := 1;
                end;
  end; { case }
  while ((X + Dx) >= 1) and ((X + Dx) <= n) and
        ((Y + DY) >= 1) and ((Y + DY) <= n) and
        (Board[X + Dx, Y + Dy] <> Empty)   and { go to topmost, leftmost }
        (Board[X + Dx, Y + Dy] = Piece ) do
  begin
     X := X + Dx;
     Y := Y + Dy;
  end;
  BlinkRow(X, Y, Dx, Dy);
  X := XHold;                             { restore winning position }
  Y := YHold;
  GotoSquare(X, Y);                      { go back to winning square }
  TextColor(NormalColor);
end; { BlinkWinner }

procedure MakeMove(X, Y : IndexType);
{ Performs the move X,Y for player, and updates the global variables
  (Board, Line, Value, Player, GameWon, TotalLines and the screen)   }

var
  Opponent : ColorType;
  X1 ,Y1   : integer;
  K, L     : NumberType;
  WinningLine : TypeOfWin;

procedure Add(var Num : NumberType);
{ Adds one to the number of pieces in a line }
begin
  Num := Num + 1;                  { Adds one to the number.     }
  if Num = 1 then                  { If it is the first piece in }
    TotalLines := TotalLines - 1;  { the line, then the opponent }
                                   { cannot use it any more.     }
  if Num = 5 then                  { The game is won if there    }
    GameWon := true;               { are 5 in line.              }
end; { Add }

procedure Update(Lin : LineType; var Valu : ValueType);
{ Updates the value of a square for each player, taking into
  account that player has placed an extra piece in the square.
  The value of a square in a usable line is Weight[Lin[Player]+1]
  where Lin[Player] is the number of pieces already placed
  in the line }
begin
  { If the opponent has no pieces in the line, then simply
    update the value for player }
  if Lin[Opponent] = 0 then
    Valu[Player] := Valu[Player] +
                       Weight[Lin[Player] + 1] - Weight[Lin[Player]]
  else
    { If it is the first piece in the line, then the line is
      spoiled for the opponent }
    if Lin[Player] = 1 then
      Valu[Opponent] := Valu[Opponent] - Weight[Lin[Opponent] + 1];
end; { Update }

begin  { MakeMove }
  WinningLine := Null;
  Opponent := OpponentColor(Player);
  GameWon := false;

  { Each square of the board is part of 20 different lines.
    The procedure adds one to the number of pieces in each
    of these lines. Then it updates the value for each of the 5
    squares in each of the 20 lines. Finally Board is updated, and
    the move is printed on the screen. }

  for K := 0 to 4 do           { Horizontal lines, from left to right }
  begin
    X1 := X - K;                           { Calculate starting point }
    Y1 := Y;
    if (1 <= X1) and (X1 <= N - 4) then        { Check starting point }
    begin
      Add(Line[0, X1, Y1, Player]);                 { Add one to line }
      if GameWon and (WinningLine = Null) then    { Save winning line }
        WinningLine := Horiz;
      for L := 0 to 4 do { Update value for the 5 squares in the line }
        Update(Line[0, X1, Y1], Value[X1 + L, Y1]);
    end;
  end; { for }

  for K := 0 to 4 do { Diagonal lines, from lower left to upper right }
  begin
    X1 := X - K;
    Y1 := Y - K;
    if (1 <= X1) and (X1 <= N - 4) and
       (1 <= Y1) and (Y1 <= N - 4) then
    begin
      Add(Line[1, X1, Y1, Player]);
      if GameWon and (WinningLine = Null) then    { Save winning line }
        WinningLine := DownLeft;
      for L := 0 to 4 do
        Update(Line[1, X1, Y1], Value[X1 + L, Y1 + L]);
    end;
  end; { for }

  for K := 0 to 4 do       { Diagonal lines, down right to upper left }
  begin
    X1 := X + K;
    Y1 := Y - K;
    if (5 <= X1) and (X1 <= N) and
       (1 <= Y1) and (Y1 <= N - 4) then
    begin
      Add(Line[3, X1, Y1, Player]);
      if GameWon and (WinningLine = Null) then    { Save winning line }
        WinningLine := DownRight;
      for L := 0 to 4 do
        Update(Line[3, X1, Y1], Value[X1 - L, Y1 + L]);
    end;
  end; { for }

  for K := 0 to 4 do                { Vertical lines, from down to up }
  begin
    X1 := X;
    Y1 := Y - K;
    if (1 <= Y1) and (Y1 <= N - 4) then
    begin
      Add(Line[2, X1, Y1, Player]);
      if GameWon and (WinningLine = Null) then    { Save winning line }
        WinningLine := Vert;
      for L := 0 to 4 do
        Update(Line[2, X1, Y1], Value[X1, Y1 + L]);
    end;
  end; { for }

  Board[X, Y] := Player;             { Place piece in board }
  if GameWon then
    BlinkWinner(Player, X, Y, WinningLine)
  else
    PrintMove(Player, X, Y);         { Print move on screen }
  Player := Opponent;        { The opponent is next to move }
end; { MakeMove }

function GameOver : boolean;
{ A game is over if one of the players have
  won, or if there are no more empty lines }
begin
  GameOver := GameWon or (TotalLines <= 0);
end; { GameOver }

procedure FindMove(var X, Y : IndexType);
{ Finds a move X,Y for player, simply by
  picking the one with the highest value }
var
  Opponent  : ColorType;
  I, J      : IndexType;
  Max, Valu : integer;
begin
  Opponent := OpponentColor(Player);
  Max := -MaxInt;
  { If no square has a high value then pick the one in the middle }
  X := (N + 1) DIV 2;
  Y := (N + 1) DIV 2;
  if Board[X, Y] = Empty then Max := 4;
  { The evaluation for a square is simply the value of the square
    for the player (attack points) plus the value for the opponent
    (defense points). Attack is more important than defense, since
    it is better to get 5 in line yourself than to prevent the op-
    ponent from getting it. }

   for I := 1 to N do { For all empty squares }
     for J := 1 to N do
       if Board[I, J] = Empty then
       begin
         { Calculate evaluation }
         Valu := Value[I, J, Player] * (16 + AttackFactor) DIV
                 16 + Value[I, J, Opponent] + Random(4);
         if Valu > Max then { Pick move with highest value }
         begin
           X := I;
           Y := J;
           Max := Valu;
         end;
       end; { if }
end; { FindMove }

procedure ClearBuffer;
{ Clear the keyboard buffer }
var
  Ch : char;
begin
  While KeyPressed do
    Ch := ReadKey;
end; { ClearBuffer }

procedure GetChar(var Ch : char);
{ Get a character from the keyboard }
begin
  Ch := UpCase(ReadKey);
end; { GetChar }

procedure ReadCommand(X, Y : IndexType; var Command : char);
{ Reads in a valid command character }
var
  ValidCommand : boolean;

begin
  repeat
    ValidCommand := true;
    GotoSquare(X, Y);                                    { Goto square }
    GetChar(Command);                             { Read from keyboard }
    case Command of
      '?'      : Help;                           { ... Help system ... }
      CtrlC    : Command := 'Q';                   { Ctrl-C means quit }
      Return,                          { Return or space means place a }
      Space    : Command := 'E';       { stone at the cursor position  }
      NullKey  : begin
                   GetChar(Command);
                   case Command of
                     'K' : Command := 'L';             { Left arrow  }
                     'M' : Command := 'R';             { Right arrow }
                     'P' : Command := 'D';             { Down arrow  }
                     'H' : Command := 'U';             { Up arrow    }
                     'G' : Command := '7';             { Home key    }
                     'I' : Command := '9';             { PgUp key    }
                     'O' : Command := '1';             { End key     }
                     'Q' : Command := '3';             { PgDn key    }
                     else
                     begin
                       ValidCommand := false;
                       ClearBuffer;
                     end; { case else }
                   end; { case }
                 end;
      Esc : if GameOver then command := 'P' { GameOver? treat Esc }
            else                            { like any other key  }
            begin
              ValidCommand := false;     { ignore Esc during game }
              ClearBuffer;
            end; { ignore Esc }
      'N','Q','A','P','H' : ;
      else
      begin
        ValidCommand := false;
        ClearBuffer;
      end; { case else }
    end; { case }
  until ValidCommand;
end; { ReadCommand }

procedure Initialize;
begin
  ClrScr;
  Randomize;
  AutoPlay := false;
  FileRead := false; { Help file not read yet }
end; { Initialize }

procedure InterpretCommand(Command : char);
var
  Temp : integer;
begin
  case Command of
    'N': begin                                        { Start new game }
           ResetGame(false);     { ResetGame but only redraw the board }
           X := (N + 1) DIV 2;
           Y := X;
         end;
    'H': FindMove(X, Y);               { Give the user a hint }
    'L': X := (X + N - 2) MOD N + 1;                  { Left  }
    'R': X := X MOD N + 1;                            { Right }
    'D': Y := (Y + N - 2) MOD N + 1;                  { Down  }
    'U': Y := Y MOD N + 1;                            { Up    }
    '7': begin
           if (X = 1) or (Y = N) then    { Move diagonally    }
           begin                         { towards upper left }
             Temp := X;
             X := Y;
             Y := Temp;
           end
           else
           begin
             X := X - 1;
             Y := Y + 1;
           end;
         end;
    '9': begin                           { Move diagonally    }
           if X = N then                 { toward upper right }
           begin
             X := (N - Y) + 1;
             Y := 1;
           end
           else if Y = N then
           begin
             Y := (N - X) + 1;
             X := 1;
           end
           else
           begin
             X := X + 1;
             Y := Y + 1;
           end
         end;
    '1': begin                            { Move diagonally   }
           if Y = 1 then                  { toward lower left }
           begin
             Y := (N - X) + 1;
             X := N;
           end
           else if X = 1 then
           begin
             X := (N - Y) + 1;
             Y := N;
           end
           else
           begin
             X := X - 1;
             Y := Y - 1;
           end;
         end;
    '3': begin                           { Move diagonally    }
           if (X = N) or (Y = 1) then    { toward lower right }
           begin
             Temp := X;
             X := Y;
             Y := Temp;
           end
           else
           begin
             X := X + 1;
             Y := Y - 1;
           end;
         end;
    'A': AutoPlay := true;                   { Auto play mode }
  end; { case }
end; { InterpretCommand }

procedure PlayerMove;
{ Enter and make a move }
begin
  if Board[X, Y] = Empty then
  begin
    MakeMove(X, Y);
    if GameWon then
      PrintMsg('Congratulations, You won!');
    Command := 'P';
  end;
end; { PlayerMove }

procedure ProgramMove;
{ Find and perform programs move }
begin
  repeat
    if KeyPressed then
      ClearBuffer;
    if GameOver then
    begin
      AutoPlay := false;
      if (Command <> 'Q') and (not GameWon) then
        PrintMsg('Tie game!');
    end
    else
    begin
      FindMove(X, Y);
      MakeMove(X, Y);
      if GameWon then
        PrintMsg('I won!');
    end;
  until AutoPlay = false;
end; { ProgramMove }

begin { Program Body }
  Initialize;
  ResetGame(true);     { ResetGame and draw the entire screen }
  X := (N + 1) DIV 2;              { Set starting position to }
  Y := X;                          { the middle of the board  }
  repeat
    ReadCommand(X, Y, Command);
    if GameOver then
      if Command <> 'Q' then
        Command := 'N';
    InterpretCommand(Command);
    if Command = 'E' then
      PlayerMove;
    if Command in ['P', 'A'] then
      ProgramMove;
  until Command in ['Q', CtrlC];
  Abort;
end.
