function A = ConvertGephiEdgeListToAdjacencyMatrices(edgesList, d, n, columnsSeparator, dimensionsSeparator)
% Generates a cell array of adjacency matrices from the provided edge list file.
% The edge list file is supported to have the following format:
% Source; Target; Label; Type
% where label is of the following format : d0, .., dy, dz. where di represents the dimension index.
% Example : '1; 2; 0,2; Undirected' means the that the pair of nodes (1,2) is connected by an dimensions 0 and 2.
% The file must have the header 'Source; Target; Label; Type'
% Note that the direction of the nodes isn't considered meaning that all elements of A are symetric.
% edgesList: An array of strings where each elements represents a line (Including the header) of the Edge List textual file.
% d: Number of dimensions of the network.
% n: Number of nodes of the network.
% columnsSeparator: ';' by default. Represents the separator between the columns of the edge list.
% dimensionsSeparator: ',' by default. Represents the separator between the dimensions printed on the label of the edge.

	% Setting up default values.
	if (isempty(columnsSeparator))
		columnsSeparator = ';';
	end
	if (isempty(dimensionsSeparator))
		dimensionsSeparator = ',';
	end

	% Initialializing the cell array.
	A={};
	for i=1 : d
		A{i} = zeros(n,n);
    end

	% Now parsing the contents
	% Ignoring the header.
	for i=2 : length(edgesList)
		currentEdge = edgesList{i};
		columns = strsplit(currentEdge, columnsSeparator);
		
		source = str2num(columns{1});
		destination = str2num(columns{2});
		
		dimensions = strsplit(columns{3}, dimensionsSeparator);
		for j=1 : length(dimensions)
            dimensionIndex = str2num(dimensions{j});
			if A{dimensionIndex}(source, destination) == 1 || A{dimensionIndex}(destination, source) == 1
				continue;
			end
			
			A{dimensionIndex}(source, destination) = 1;
			A{dimensionIndex}(destination, source) = 1;
		end
	end
end