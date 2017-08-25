function [] = ConvertAdjacencyMatricesToGephiEdgeList(A, columnsSeparator, dimensionsSeparator)
% Converts a list of adjacency matrices to a list of edges in the format : source; target; d0, .., dx, dy, dz; undirected
% which can be imported in the data laboratory of Gephi. The third column is indeed the label of the edge.
% Dimension indices represent the index of the corresponding layer in the cell array of adjacency matrices A.
% Example: 1;2;1,5;Undirected means that the pair of nodes (1, 2) is connected by dimensions 1 and 5.
% Note that the direction of the edges is ignored.
% This format was adopted since Gephi 0.8.2  does not offer any support for multigraphs and the latest version which
% offers such a supports  (0.9.1) unfortunately doesn't provide a clustering module.
% A: A cell array of adjacency matrices. Each cell describes the adjacency matrix Ai of layer i of the multidimensional network.
% dimensionsSeparator: ',' by default. Used to separate the dimensions on the edge labels.

if (isempty(dimensionsSeparator))
	dimensionsSeparator = ',';
end

if (isempty(columnsSeparator))
	columnsSeparator = ';';
end

% Generating the list of node ids.
file = fopen('NodesList.txt', 'wt');
fprintf(file, '%s\n', strcat('Id', columnsSeparator, 'Label'));

for i=1 : length(A{1,1})
	fprintf(file, '%s\n', strcat(num2str(i),columnsSeparator, num2str(i)));
end
fclose(file);

% Now the edges list. Format is the following (Assuming the dimensions label dimensionsSeparator is a comma)
% Source; Target; d0, .., dx, dy, dz; undirected

file = fopen('GephiEdgesList.txt', 'wt');
fprintf(file, '%s\n', strcat('Source', columnsSeparator, 'Target', columnsSeparator, 'Label', columnsSeparator, 'Type'));

dimensionsCount = length(A);
for i=1 : length(A{1,1})
	for j=i+1 : length(A{1,1})
		label = '';
		for k=1 : dimensionsCount
			if (A{1,k}(i,j) > 0 || A{1,k}(j,i) > 0) % dimension k connects i and j
				label = strcat(label, dimensionsSeparator, num2str(k));
			end
		end
		
		if (~isempty(label))
			if (label(1) == dimensionsSeparator);
				label = label(2:length(label)); %trim left the first dimensionsSeparator character.
			end
			% printing the edge.
			% Source; Target; Dimensions; undirected;
			ijk = strcat(num2str(i), columnsSeparator, num2str(j), columnsSeparator , label, columnsSeparator, 'undirected');
			fprintf(file, '%s\n', ijk);
		end 
   end
end

fclose(file);