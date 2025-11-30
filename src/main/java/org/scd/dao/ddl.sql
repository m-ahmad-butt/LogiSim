-- Projects table
CREATE TABLE Project (
                         projectID INTEGER PRIMARY KEY AUTOINCREMENT,
                         projectName TEXT NOT NULL
);

-- Circuits table
CREATE TABLE Circuit (
                         circuitID INTEGER PRIMARY KEY AUTOINCREMENT,
                         projectID INTEGER NOT NULL,
                         circuitName TEXT NOT NULL,
                         FOREIGN KEY (projectID) REFERENCES Project(projectID) ON DELETE CASCADE
);

-- Components table
CREATE TABLE Component (
                           component_id INTEGER PRIMARY KEY AUTOINCREMENT,
                           circuit_id INTEGER NOT NULL,
                           component_type TEXT NOT NULL,
                           positionX REAL NOT NULL,
                           positionY REAL NOT NULL,
                           component_output INTEGER,
                           FOREIGN KEY (circuit_id) REFERENCES Circuit(circuitID) ON DELETE CASCADE
);

-- Component inputs table
CREATE TABLE Component_Input (
                                 input_id INTEGER PRIMARY KEY AUTOINCREMENT,
                                 component_id INTEGER NOT NULL,
                                 input_value INTEGER,
                                 input_order INTEGER,
                                 FOREIGN KEY (component_id) REFERENCES Component(component_id) ON DELETE CASCADE
);

-- Connectors table
CREATE TABLE Connector (
                           connector_id INTEGER PRIMARY KEY AUTOINCREMENT,
                           component_color TEXT,
                           source_id INTEGER NOT NULL,
                           sink_id INTEGER NOT NULL,
                           FOREIGN KEY (source_id) REFERENCES Component(component_id) ON DELETE CASCADE,
                           FOREIGN KEY (sink_id) REFERENCES Component(component_id) ON DELETE CASCADE
);