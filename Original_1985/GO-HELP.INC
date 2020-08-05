{                             GO-HELP.INC
                              GO-MOKU 4.0
            Copyright (c) 1985, 87 by Borland International, Inc.

  Note: To incorporate this system into other programs,  modify
        and include this file and all statements in GO-MOKU.PAS
        marked with a comment contain:  ... Help system ...
}

procedure Help;
const
  MaxLines = 50;                 { Max # lines read into help screen }
  FirstCol = 50;                            { Inside the help window }
  FirstRow =  8;
  LastCol  = 75;
  LastRow  = 21;
  HelpFileName : string[20] = 'GO-MOKU.HLP';

type
  RowType     = string[25];
  ScreenArray = array[1..MaxLines] of RowType;    { Stores help text }

var
  HelpFile : Text;                                   { The help file }
  HelpText : ScreenArray;                { Storage for the help file }
  LastLineRead : integer;    { The last line read from the help file }
  BorderTop, BorderBottom,           { Help screen border boundaries }
  BorderLeft, BorderRight : byte;

procedure DrawBorder;
{ Draw a border around the help window }
const
  VerticalBorder = #186; HorizontalBorder = #205;
  UpLeft         = #201; UpRight          = #187;
  DownLeft       = #200; DownRight        = #188;
var
  Row, Col : integer;
begin
  BorderTop := 1;  BorderBottom := 16;
  BorderLeft := 1; BorderRight := 29;
  ClrScr;
  TextColor(NormalColor - 8);
  GotoXY(BorderLeft, BorderTop);
  Write(UpLeft);
  for Col := BorderLeft + 1 to BorderRight - 22 do
    Write(HorizontalBorder);
  TextColor(NormalColor);
  Write(' GOMOKU HELP ');
  TextColor(NormalColor - 8);
  for Col := BorderLeft + 20 to BorderRight - 1 do
    Write(HorizontalBorder);
  Write(UpRight);
  for Row := BorderTop + 1 to BorderBottom - 1 do
  begin
    GotoXY(BorderLeft, Row);
    Write(VerticalBorder);
    GotoXY(BorderRight, Row);
    Write(VerticalBorder);
  end;
  GotoXY(BorderLeft, BorderBottom);
  Write(DownLeft);
  for Col := BorderLeft + 1 to BorderRight - 1 do
    Write(HorizontalBorder);
  Write(DownRight);
end; { DrawBorder }

procedure DrawHelpLine;
{ Draws a help line below the help window }
begin
  GotoXY(BorderLeft + 1, BorderBottom + 1);
  WriteHelp(#24 + #196, 1);
  WriteHelp(#25 + #196 + 'Scroll   ', 1);
  WriteHelp('PgUp' + #196, 4);
  WriteHelp('PgDn' + #196 + 'Page', 4);
end; { DrawHelpLine }

function ReadHelpFile : boolean;
{ Reads in the help file and returns
  true if the read was successful    }
var
  Ch : char;

procedure LoadArray;
{ Stores the help file into an array }
begin
  GotoXY(1, 2);
  ClrEOL;
  WriteLn(' Reading Text . . .');
  FillChar(HelpText, SizeOf(HelpText), #0); { Initialize array }
  LastLineRead := 0;
  while not EOF(HelpFile) and (LastLineRead < MaxLines) do
  begin
    LastLineRead := LastLineRead + 1;
    ReadLn(HelpFile, HelpText[LastLineRead]);
  end;
  if not EOF(HelpFile) then
  begin
    WriteLn;
    WriteLn;
    WriteLn('Sorry, this program only');
    WriteLn('reads ', MaxLines, ' lines of text.');
  end;
end; { LoadArray }

begin { ReadHelpFile }
  Window(FirstCol, FirstRow, LastCol, LastRow);
  GotoXY(1, 1);
  ClrScr;
  GotoXY(1, 1);
  WriteLn;
  Write('Looking for ', HelpFileName);
  Assign(HelpFile, HelpFileName);
  {$I-}
  Reset(HelpFile);
  {$I+}
  if IOresult <> 0 then
  begin
    GotoXY(1, 2);
    ClrEOL;
    WriteLn(HelpFileName, ' not found.');
    Write('Press <ESC> ');
    Ch := ReadKey;
    ReadHelpFile := false;
  end
  else
  begin
    LoadArray;
    if LastLineRead < 1 then
    begin
      ClrScr;
      GotoXY(1, 2);
      ClrEOL;
      WriteLn('The help file is empty.');
      Write('Press <ESC> ');
      Ch := ReadKey;
      ReadHelpFile := false;
    end
    else
    begin
      ReadHelpFile := true;
      FileRead := true;
    end;
  end;
end; { ReadHelpFile }

procedure Displayfile;
{ Display the help file in the help window }
const
  PgDn   = #81;
  PgUp   = #73;
  ScrlDn = #80;
  ScrlUp = #72;
  Esc    = #27;
  NullKey   = #0;
var
  TopLine, BottomLine, MaxRows   : integer;
  PgCommand                      : char;

procedure DisplayPage(TopLine, BottomLine : integer);
{ Display a page of text in the help window }
var
  Row : integer;
begin
  ClrScr;
  for Row := TopLine to BottomLine do
  begin
    if Row <> BottomLine then
      WriteLn(HelpText[row])
    else
      Write(HelpText[row]);
  end;
  GotoXY(1, LastRow);
end; { DisplayPage }

procedure PageDown(var TopLine, BottomLine : integer);
{ Page down in the help window }
begin
  if BottomLine + 1 <= LastLineRead then
  begin
    if (BottomLine + MaxRows) > LastLineRead then
      BottomLine := LastLineRead
    else
      BottomLine := BottomLine + MaxRows;
    if (BottomLine - MaxRows + 1) >= 1 then
      TopLine := (BottomLine - MaxRows + 1)
    else
      TopLine := 1;
    DisplayPage(TopLine, BottomLine);
  end;
end; { PageDown }

procedure PageUp(var TopLine, BottomLine : integer);
{ Page up in the help window }
begin
  if TopLine > 1 then
  begin
    if (TopLine - MaxRows) > 1 then                 { Set TopLine    }
      TopLine := TopLine - MaxRows
    else
      TopLine := 1;
    if (TopLine + MaxRows - 1) > LastLineRead then  { Set BottomLine }
      BottomLine := LastLineRead
    else
      BottomLine := TopLine + MaxRows - 1;
    DisplayPage(TopLine, BottomLine);
  end;
end; { PageUp }

procedure ScrollUp(var TopLine, BottomLine : integer);
{ Scroll the help screen up one line }
begin
  if TopLine > 1 then
  begin
    if (BottomLine - TopLine + 1) >= MaxRows then  { screen was full }
      BottomLine := BottomLine - 1;
    TopLine := TopLine - 1;
    GotoXY(1, MaxRows);
    DelLine;
    GotoXY(1, 1);
    InsLine;
    Write(HelpText[TopLine]);
    GotoXY(1, MaxRows);
  end;
end; { ScrollUp }

procedure ScrollDown(var TopLine, BottomLine : integer);
{ Scroll the help screen down one line }
begin
  if BottomLine < LastLineRead then
  begin
    TopLine := TopLine + 1;
    BottomLine := BottomLine + 1;
    GotoXY(1, 1);
    DelLine;
    GotoXY(1, MaxRows);
    InsLine;
    Write(HelpText[BottomLine]);
    GotoXY(1, MaxRows);
  end;
end; { ScrollDown }

procedure Init;
{ Initialization routine }
begin
  MaxRows := LastRow - FirstRow + 1;
  TopLine := 1;
  if MaxRows > LastLineRead then
    BottomLine := LastLineRead
  else
    BottomLine := MaxRows;
  DisplayPage(TopLine, BottomLine);  { Show first page }
end; { Init }

begin { Displayfile }
  Init;
  repeat
    PgCommand := ReadKey;
    PgCommand := UpCase(PgCommand);
    if (PgCommand = CtrlC) or (PgCommand = 'Q') then
      Abort;
    if (PgCommand = NullKey) then
    begin
      PgCommand := ReadKey;
      PgCommand := UpCase(PgCommand);
      case PgCommand of
        PgDn   : PageDown(TopLine, BottomLine);
        PgUp   : PageUp(TopLine, BottomLine);
        ScrlDn : ScrollDown(TopLine, BottomLine);
        ScrlUp : ScrollUp(TopLine, BottomLine);
      end;
    end;
  until (PgCommand = Esc);
end; { Displayfile }

begin { Help }
  GotoXY(49, 5);                     { Display help commands }
  WriteHelp('ESC-Exits Help', 3);
  Window(FirstCol - 2, FirstRow - 1, LastCol + 1, LastRow + 2);
  GotoXY(1, 1);
  DrawBorder;
  DrawHelpLine;
  if FileRead then                            { Display help }
  begin
    Window(FirstCol, FirstRow, LastCol, LastRow);
    GotoXY(1, 1);
    Displayfile;
  end
  else
  if ReadHelpFile then                { Help read from disk?  }
    Displayfile;
  Window(FirstCol - 2, FirstRow - 1,           { Close window }
         LastCol + 1, LastRow + 2);
  GotoXY(1, 1);
  ClrScr;
  Window(1, 1, 80, 25);                      { Restore screen }
  GotoXY(1, 1);
  GotoXY(49, 5);
  WriteHelp('?-for Help    ', 1);              { Restore menu }
end; { Help }

{ ================ End Of On-Line Help Module =============== }
