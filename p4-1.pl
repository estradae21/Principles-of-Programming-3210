% The row below an empty sequence is an empty sequence
rowBelow([], []).

% The row below a singleton sequence is just that term
rowBelow([Xn], Xn).

% The row below a sequence of 2 terms is the singleton sequence (X2-X1)
rowBelow([X1 , X2], [Xn]) :- Xn is X2 - X1.

% Let (x_n):=(x_1, x_2, ..., x_n) be a sequence of integers. The row below x_n
% is defined as the sequence whose terms satisfy y_i:= x_i+1 - x_i for all
% 1 <= i <= n
rowBelow([X1, X2 | Xn_2], [Y1 | Ym]) :- Y1 is X2 - X1, rowBelow([X2 | Xn_2], Ym).

% Accessor of the singleton term at the bottom of the heirarchy of lists
getSingleton([Xn] , Xn).

% Recursively return the heirarchy of rows until the singleton row is reached
getSingleton([X1 | Xn], Singleton) :- getSingleton(Xn , Singleton).

% If the sequence is a singleton sequence, then this is the identity case; i.e.
% the next term is the inputted term itself.
nextItem([E], E).

% Let x_n := (x_1, x_2, ..., x_n) be a sequence of integers. 
% Let y_n := (x_2 - x_1, x_3 - x_2, ..., x_n - x_{n-1}) be the "row below" x_n,
% and let z_n be the sequence of mth terms of each of the rows below x_n.
% Then the next term of (x_n) is calculated as Σ_{i=1}^n z_i
%
% In another interpretation, let f(x,y):= x if y=[], and f(x,y):=y-x otherwise.
% Then we have the permutation
% A := (f(x_1,x_2), f^2 (x_1,x_2), ..., f^{n-1} (x_1,x_2)) = (a_1, a_2, ..., a_m)
%
% Let the list of permutations A be defined as
% α := (A^{m-1} (a_1), A^{m-2} (a_1), ..., A(a_1), a_1)
%
% Then we have that the next term in the sequence is calculated as the sum of
% the last terms of each of the sequences in the permutation α.
nextItem([A | An_1], NextTerm) :-
	rowBelow([A | An_1], Y1),
	getSingleton([A | An_1], LastTerm),
	nextItem(Y1, Z1),
	NextTerm is Z1 + LastTerm.
