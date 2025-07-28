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
    
    // Debug: Log the endpoint for troubleshooting
    error_log("Requested endpoint: " . $endpoint);
    error_log("Request method: " . $method);
    error_log("Full path: " . $path);
    
    // Handle different endpoints
    switch ($method) {
        case 'GET':
            if ($endpoint === 'telecom_plans') {
                getTelecomPlans($pdo);
            } elseif (is_numeric($endpoint) || strpos($endpoint, 'plan_') === 0) {
                getTelecomPlanById($pdo, $endpoint);
            } else {
                // For any GET request to this file, return all plans
                getTelecomPlans($pdo);
            }
            break;
            
        case 'POST':
            if ($endpoint === 'telecom_plans') {
                createTelecomPlan($pdo);
            } else {
                // For any POST request to this file, create a plan
                createTelecomPlan($pdo);
            }
            break;
            
        case 'PUT':
            if (is_numeric($endpoint) || strpos($endpoint, 'plan_') === 0) {
                updateTelecomPlan($pdo, $endpoint);
            } else {
                http_response_code(404);
                echo json_encode(['error' => 'Plan ID required for PUT request']);
            }
            break;
            
        case 'DELETE':
            if (is_numeric($endpoint) || strpos($endpoint, 'plan_') === 0) {
                deleteTelecomPlan($pdo, $endpoint);
            } else {
                http_response_code(404);
                echo json_encode(['error' => 'Plan ID required for DELETE request']);
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

// Function to get all telecom plans
function getTelecomPlans($pdo) {
    try {
        // Check for carrier filter
        $carrier = $_GET['carrier'] ?? null;
        
        if ($carrier) {
            $query = "SELECT * FROM telecom_plans WHERE carrier_name LIKE :carrier ORDER BY price ASC";
            $stmt = $pdo->prepare($query);
            $carrierParam = "%$carrier%";
            $stmt->bindParam(':carrier', $carrierParam, PDO::PARAM_STR);
        } else {
            $query = "SELECT * FROM telecom_plans ORDER BY price ASC";
            $stmt = $pdo->prepare($query);
        }
        
        $stmt->execute();
        $plans = $stmt->fetchAll(PDO::FETCH_ASSOC);
        
        echo json_encode([
            'success' => true,
            'data' => $plans,
            'count' => count($plans)
        ]);
        
    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode([
            'error' => 'Failed to fetch telecom plans',
            'message' => $e->getMessage()
        ]);
    }
}

// Function to get telecom plan by ID
function getTelecomPlanById($pdo, $id) {
    try {
        $query = "SELECT * FROM telecom_plans WHERE id = :id";
        $stmt = $pdo->prepare($query);
        $stmt->bindParam(':id', $id, PDO::PARAM_STR);
        $stmt->execute();
        $plan = $stmt->fetch(PDO::FETCH_ASSOC);
        
        if ($plan) {
            echo json_encode([
                'success' => true,
                'data' => $plan
            ]);
        } else {
            http_response_code(404);
            echo json_encode([
                'error' => 'Telecom plan not found'
            ]);
        }
        
    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode([
            'error' => 'Failed to fetch telecom plan',
            'message' => $e->getMessage()
        ]);
    }
}

// Function to create new telecom plan
function createTelecomPlan($pdo) {
    try {
        $input = json_decode(file_get_contents('php://input'), true);
        
        // Validate required fields
        if (!isset($input['id']) || !isset($input['name']) || !isset($input['price']) || !isset($input['data'])) {
            http_response_code(400);
            echo json_encode(['error' => 'id, name, price, and data are required']);
            return;
        }
        
        $query = "INSERT INTO telecom_plans (id, name, price, data, carrier_name, plan_type, contract_length, features) 
                  VALUES (:id, :name, :price, :data, :carrier_name, :plan_type, :contract_length, :features)";
        
        $stmt = $pdo->prepare($query);
        $stmt->bindParam(':id', $input['id']);
        $stmt->bindParam(':name', $input['name']);
        $stmt->bindParam(':price', $input['price']);
        $stmt->bindParam(':data', $input['data']);
        $stmt->bindParam(':carrier_name', $input['carrier_name'] ?? null);
        $stmt->bindParam(':plan_type', $input['plan_type'] ?? 'POSTPAID');
        $stmt->bindParam(':contract_length', $input['contract_length'] ?? null, PDO::PARAM_INT);
        $stmt->bindParam(':features', $input['features'] ?? null);
        
        $stmt->execute();
        
        echo json_encode([
            'success' => true,
            'message' => 'Telecom plan created successfully',
            'id' => $input['id']
        ]);
        
    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode([
            'error' => 'Failed to create telecom plan',
            'message' => $e->getMessage()
        ]);
    }
}

// Function to update telecom plan
function updateTelecomPlan($pdo, $id) {
    try {
        $input = json_decode(file_get_contents('php://input'), true);
        
        $query = "UPDATE telecom_plans SET 
                  name = :name,
                  price = :price,
                  data = :data,
                  carrier_name = :carrier_name,
                  plan_type = :plan_type,
                  contract_length = :contract_length,
                  features = :features
                  WHERE id = :id";
        
        $stmt = $pdo->prepare($query);
        $stmt->bindParam(':id', $id);
        $stmt->bindParam(':name', $input['name'] ?? '');
        $stmt->bindParam(':price', $input['price'] ?? 0);
        $stmt->bindParam(':data', $input['data'] ?? '');
        $stmt->bindParam(':carrier_name', $input['carrier_name'] ?? null);
        $stmt->bindParam(':plan_type', $input['plan_type'] ?? 'POSTPAID');
        $stmt->bindParam(':contract_length', $input['contract_length'] ?? null, PDO::PARAM_INT);
        $stmt->bindParam(':features', $input['features'] ?? null);
        
        $stmt->execute();
        
        if ($stmt->rowCount() > 0) {
            echo json_encode([
                'success' => true,
                'message' => 'Telecom plan updated successfully'
            ]);
        } else {
            http_response_code(404);
            echo json_encode([
                'error' => 'Telecom plan not found'
            ]);
        }
        
    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode([
            'error' => 'Failed to update telecom plan',
            'message' => $e->getMessage()
        ]);
    }
}

// Function to delete telecom plan
function deleteTelecomPlan($pdo, $id) {
    try {
        $query = "DELETE FROM telecom_plans WHERE id = :id";
        $stmt = $pdo->prepare($query);
        $stmt->bindParam(':id', $id);
        $stmt->execute();
        
        if ($stmt->rowCount() > 0) {
            echo json_encode([
                'success' => true,
                'message' => 'Telecom plan deleted successfully'
            ]);
        } else {
            http_response_code(404);
            echo json_encode([
                'error' => 'Telecom plan not found'
            ]);
        }
        
    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode([
            'error' => 'Failed to delete telecom plan',
            'message' => $e->getMessage()
        ]);
    }
}
?> 