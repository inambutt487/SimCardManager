<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization');

// Handle preflight OPTIONS request
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

// Database configuration
$host = 'localhost';
$dbname = 'directdevhub_task';
$username = 'directdevhub_task';
$password = 'Allah786@2025';

try {
    // Create PDO connection
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8mb4", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    // Get request method and parameters
    $method = $_SERVER['REQUEST_METHOD'];
    $path = parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH);
    $path_parts = explode('/', trim($path, '/'));
    $endpoint = end($path_parts);
    
    // Remove .php extension if present
    $endpoint = str_replace('.php', '', $endpoint);
    
    // Handle different endpoints
    switch ($method) {
        case 'GET':
            if ($endpoint === 'simcards') {
                getSimCards($pdo);
            } elseif (is_numeric($endpoint)) {
                getSimCardById($pdo, $endpoint);
            } else {
                http_response_code(404);
                echo json_encode(['error' => 'Endpoint not found']);
            }
            break;
            
        case 'POST':
            if ($endpoint === 'simcards') {
                createSimCard($pdo);
            } else {
                http_response_code(404);
                echo json_encode(['error' => 'Endpoint not found']);
            }
            break;
            
        case 'PUT':
            if (is_numeric($endpoint)) {
                updateSimCard($pdo, $endpoint);
            } else {
                http_response_code(404);
                echo json_encode(['error' => 'Endpoint not found']);
            }
            break;
            
        case 'DELETE':
            if (is_numeric($endpoint)) {
                deleteSimCard($pdo, $endpoint);
            } else {
                http_response_code(404);
                echo json_encode(['error' => 'Endpoint not found']);
            }
            break;
            
        default:
            http_response_code(405);
            echo json_encode(['error' => 'Method not allowed']);
            break;
    }
    
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode([
        'error' => 'Database connection failed',
        'message' => $e->getMessage()
    ]);
}

// Function to get all simcards
function getSimCards($pdo) {
    try {
        $query = "SELECT * FROM simcards ORDER BY slot_number, created_at DESC";
        $stmt = $pdo->prepare($query);
        $stmt->execute();
        $simcards = $stmt->fetchAll(PDO::FETCH_ASSOC);
        
        echo json_encode([
            'success' => true,
            'data' => $simcards,
            'count' => count($simcards)
        ]);
        
    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode([
            'error' => 'Failed to fetch simcards',
            'message' => $e->getMessage()
        ]);
    }
}

// Function to get simcard by ID
function getSimCardById($pdo, $id) {
    try {
        $query = "SELECT * FROM simcards WHERE id = :id";
        $stmt = $pdo->prepare($query);
        $stmt->bindParam(':id', $id, PDO::PARAM_INT);
        $stmt->execute();
        $simcard = $stmt->fetch(PDO::FETCH_ASSOC);
        
        if ($simcard) {
            echo json_encode([
                'success' => true,
                'data' => $simcard
            ]);
        } else {
            http_response_code(404);
            echo json_encode([
                'error' => 'Simcard not found'
            ]);
        }
        
    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode([
            'error' => 'Failed to fetch simcard',
            'message' => $e->getMessage()
        ]);
    }
}

// Function to create new simcard
function createSimCard($pdo) {
    try {
        $input = json_decode(file_get_contents('php://input'), true);
        
        // Validate required fields
        if (!isset($input['slot_number']) || !isset($input['sim_state'])) {
            http_response_code(400);
            echo json_encode(['error' => 'slot_number and sim_state are required']);
            return;
        }
        
        $query = "INSERT INTO simcards (slot_number, carrier_name, sim_state, network_type, iccid, imsi, phone_number, country_code, is_active) 
                  VALUES (:slot_number, :carrier_name, :sim_state, :network_type, :iccid, :imsi, :phone_number, :country_code, :is_active)";
        
        $stmt = $pdo->prepare($query);
        $stmt->bindParam(':slot_number', $input['slot_number'], PDO::PARAM_INT);
        $stmt->bindParam(':carrier_name', $input['carrier_name'] ?? null);
        $stmt->bindParam(':sim_state', $input['sim_state']);
        $stmt->bindParam(':network_type', $input['network_type'] ?? null);
        $stmt->bindParam(':iccid', $input['iccid'] ?? null);
        $stmt->bindParam(':imsi', $input['imsi'] ?? null);
        $stmt->bindParam(':phone_number', $input['phone_number'] ?? null);
        $stmt->bindParam(':country_code', $input['country_code'] ?? null);
        $stmt->bindParam(':is_active', $input['is_active'] ?? 0, PDO::PARAM_INT);
        
        $stmt->execute();
        $id = $pdo->lastInsertId();
        
        echo json_encode([
            'success' => true,
            'message' => 'Simcard created successfully',
            'id' => $id
        ]);
        
    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode([
            'error' => 'Failed to create simcard',
            'message' => $e->getMessage()
        ]);
    }
}

// Function to update simcard
function updateSimCard($pdo, $id) {
    try {
        $input = json_decode(file_get_contents('php://input'), true);
        
        $query = "UPDATE simcards SET 
                  slot_number = :slot_number,
                  carrier_name = :carrier_name,
                  sim_state = :sim_state,
                  network_type = :network_type,
                  iccid = :iccid,
                  imsi = :imsi,
                  phone_number = :phone_number,
                  country_code = :country_code,
                  is_active = :is_active
                  WHERE id = :id";
        
        $stmt = $pdo->prepare($query);
        $stmt->bindParam(':id', $id, PDO::PARAM_INT);
        $stmt->bindParam(':slot_number', $input['slot_number'] ?? 0, PDO::PARAM_INT);
        $stmt->bindParam(':carrier_name', $input['carrier_name'] ?? null);
        $stmt->bindParam(':sim_state', $input['sim_state'] ?? 'READY');
        $stmt->bindParam(':network_type', $input['network_type'] ?? null);
        $stmt->bindParam(':iccid', $input['iccid'] ?? null);
        $stmt->bindParam(':imsi', $input['imsi'] ?? null);
        $stmt->bindParam(':phone_number', $input['phone_number'] ?? null);
        $stmt->bindParam(':country_code', $input['country_code'] ?? null);
        $stmt->bindParam(':is_active', $input['is_active'] ?? 0, PDO::PARAM_INT);
        
        $stmt->execute();
        
        if ($stmt->rowCount() > 0) {
            echo json_encode([
                'success' => true,
                'message' => 'Simcard updated successfully'
            ]);
        } else {
            http_response_code(404);
            echo json_encode([
                'error' => 'Simcard not found'
            ]);
        }
        
    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode([
            'error' => 'Failed to update simcard',
            'message' => $e->getMessage()
        ]);
    }
}

// Function to delete simcard
function deleteSimCard($pdo, $id) {
    try {
        $query = "DELETE FROM simcards WHERE id = :id";
        $stmt = $pdo->prepare($query);
        $stmt->bindParam(':id', $id, PDO::PARAM_INT);
        $stmt->execute();
        
        if ($stmt->rowCount() > 0) {
            echo json_encode([
                'success' => true,
                'message' => 'Simcard deleted successfully'
            ]);
        } else {
            http_response_code(404);
            echo json_encode([
                'error' => 'Simcard not found'
            ]);
        }
        
    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode([
            'error' => 'Failed to delete simcard',
            'message' => $e->getMessage()
        ]);
    }
}
?> 