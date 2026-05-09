# Fraud Detection System

A comprehensive Spring Boot-based fraud detection system that analyzes financial transactions in real-time using advanced rule-based algorithms. The system provides both individual transaction analysis and batch processing capabilities through a modern web interface.

##  Features

### Core Functionality
- **Real-time Transaction Analysis**: Evaluate individual transactions instantly
- **Batch Processing**: Upload and analyze CSV files with multiple transactions
- **User Management**: Secure user registration, login, and password reset
- **Advanced Rules Engine**: 10+ sophisticated fraud detection rules
- **Risk Scoring**: Multi-level risk assessment (Safe, Low Risk, Review, High Severity Fraud)

### Fraud Detection Rules
1. **Amount Anomaly**: Detects transactions exceeding 5x average with amounts >$2000
2. **Risky Locations**: Flags transactions from high-risk countries
3. **High-Risk Merchants**: Identifies suspicious merchant categories
4. **Device Anomalies**: Detects emulator and unknown device types
5. **Velocity Checks**: Identifies rapid transaction sequences (3+ in 10 minutes)
6. **Impossible Travel**: Detects location changes within 1 hour
7. **Time Window Analysis**: Flags transactions during suspicious hours (12AM-5AM)
8. **Probing Patterns**: Detects small test transactions followed by large amounts
9. **IP Address Monitoring**: Tracks frequent IP address changes
10. **Payment Method Risk**: Evaluates wire transfer risks

### User Interface
- **Modern Web Interface**: Responsive design with dark/light theme toggle
- **Interactive Dashboard**: User-friendly transaction analysis forms
- **Real-time Results**: Instant feedback with color-coded risk levels
- **Batch Analysis**: Comprehensive CSV processing with accuracy metrics
- **Data Visualization**: Tabular results with risk categorization

##  Tech Stack

### Backend
- **Java 17**
- **Spring Boot 3.2.5**
- **Spring Data JPA** - Database operations
- **Spring Web** - REST API development
- **MySQL** - Primary database
- **Maven** - Build management

### Frontend
- **HTML5** - Structure
- **CSS3** - Styling with CSS Variables for theming
- **Vanilla JavaScript** - Interactive functionality
- **Fetch API** - HTTP requests

### Development Tools
- **JUnit 5** - Unit testing
- **Spring Boot Test** - Integration testing
- **VS Code** - Development environment

##  Prerequisites

- **Java 17** or higher
- **Maven 3.6+**
- **MySQL 8.0+**
- **Modern web browser** (Chrome, Firefox, Safari, Edge)

##  Installation & Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd fraud-detection
```

### 2. Database Setup
1. Install MySQL and create a database:
```sql
CREATE DATABASE fraud_db;
```

2. Update database credentials in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/fraud_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. Build the Application
```bash
mvn clean install
```

### 4. Run the Application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8081`

### 5. Access the Application
Open your browser and navigate to: `http://localhost:8081`

##  Usage

### User Registration & Authentication
1. **Register**: Create a new account with user ID, username, and password
2. **Login**: Access the dashboard with your credentials
3. **Password Reset**: Reset password using username

### Individual Transaction Analysis
1. Fill in transaction details:
   - Amount
   - Location (country)
   - Merchant
   - Device type
   - IP address
   - Date and time
   - Payment type
2. Click "Analyze Transaction"
3. View real-time risk assessment

### Batch CSV Analysis
1. Prepare CSV file with format:
   ```
   userId,amount,location,merchant,deviceType,ipAddress,date,time,paymentType,expectedStatus
   ```
2. Upload the CSV file
3. View comprehensive analysis with:
   - Risk distribution summary
   - Accuracy metrics (if expected status provided)
   - Detailed transaction table

### Clearing Data
- Use "Clear All Transactions" to reset the database

##  API Endpoints

### Authentication
- `POST /api/register` - User registration
- `POST /api/login` - User authentication
- `POST /api/reset-password` - Password reset

### Transactions
- `POST /api/transaction` - Analyze single transaction
- `POST /api/upload` - Batch CSV analysis
- `DELETE /api/transactions` - Clear all transactions

##  Testing

### Unit Tests
Run the test suite:
```bash
mvn test
```

### Test Coverage
The project includes comprehensive unit tests for:
- Rules engine evaluation
- Fraud detection scenarios
- Edge cases and boundary conditions

### Sample Test Data
Use `test_transactions.csv` for testing batch processing and accuracy validation.

##  Rules Engine Details

### Scoring System
- **Safe**: Score < 35
- **Low Risk**: Score 35-44
- **Review (Medium Severity)**: Score 45-79
- **Fraud (High Severity)**: Score ≥ 80

### Rule Weights
- Amount anomalies: 50 points
- Risky locations: 35 points
- High-risk merchants: 30 points
- Device anomalies: 20 points
- Velocity violations: 40 points
- Impossible travel: 35 points
- Suspicious timing: 15 points
- Probing patterns: 30 points
- IP changes: 15 points
- Wire transfers: 15 points

##  Project Structure

```
fraud-detection/
├── src/
│   ├── main/
│   │   ├── java/com/fraud/
│   │   │   ├── FraudController.java      # REST API endpoints
│   │   │   ├── FraudDetectionApplication.java # Main application class
│   │   │   ├── RulesEngine.java          # Fraud detection logic
│   │   │   ├── Transaction.java          # Transaction entity
│   │   │   ├── TransactionRepository.java # Data access layer
│   │   │   ├── User.java                 # User entity
│   │   │   └── UserRepository.java       # User data access
│   │   └── resources/
│   │       ├── application.properties    # Configuration
│   │       └── static/
│   │           ├── index.html            # Main web interface
│   │           ├── script.js             # Frontend logic
│   │           └── styles.css            # Styling and themes
│   └── test/
│       └── java/com/fraud/
│           └── RulesEngineTest.java      # Unit tests
├── scratch/                              # Utility scripts
│   ├── AccuracyCheck.java               # Accuracy testing tool
│   └── DebugRules.java                  # Rules debugging utility
├── test_transactions.csv                # Sample test data
├── pom.xml                              # Maven configuration
├── full_source_code.txt                 # Complete codebase backup
└── README.md                           # This file
```

##  Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Development Guidelines
- Follow Java naming conventions
- Add unit tests for new features
- Update documentation for API changes
- Ensure code passes all existing tests

##  License

This project is licensed under the MIT License - see the LICENSE file for details.

##  Support

For questions or issues:
- Create an issue in the repository
- Check the troubleshooting section below

##  Troubleshooting

### Common Issues

**Database Connection Failed**
- Verify MySQL is running
- Check database credentials in `application.properties`
- Ensure database `fraud_db` exists

**Application Won't Start**
- Confirm Java 17+ is installed
- Check Maven installation
- Verify port 8081 is available

**CSV Upload Issues**
- Ensure CSV has correct column format
- Check file encoding (UTF-8 recommended)
- Verify no special characters in data

**Frontend Not Loading**
- Clear browser cache
- Check console for JavaScript errors
- Ensure all static files are in `src/main/resources/static/`

##  Future Enhancements

- [ ] Machine learning integration for adaptive rules
- [ ] Real-time dashboard with live transaction monitoring
- [ ] Advanced analytics and reporting
- [ ] Multi-tenant architecture support
- [ ] API rate limiting and security enhancements
- [ ] Integration with external fraud databases
- [ ] Mobile application development
- [ ] Blockchain transaction analysis

---

**Built with ❤️ using Spring Boot**
