# 3. Decision to Implement Versioned, Immutable, and Normalized Customer Schema

## Status

**Proposed**

## Context

The initial schema design stored customer data (e.g., full name, preferred name, email address, phone number) directly within the `customer` table. While simple, this approach posed several limitations:
- **Lack of Historical Tracking**: The schema could not track changes to customer data over time.
- **Data Integrity Challenges**: There was no guarantee that customer attributes referenced the most up-to-date values.
- **Limited Flexibility**: Relationships like multiple phone numbers per customer were difficult to model.

To address these issues, a new schema design was proposed, emphasizing **versioning**, **immutability**, and **normalization**. This approach ensures:
1. **Versioning**: Every update to customer data creates a new immutable record, preserving historical changes.
2. **Immutability**: Customer data records are never overwritten or deleted, ensuring auditability and traceability.
3. **Normalization**: Key attributes are stored in separate tables, enabling flexible relationships and improved data structure.

## Decision

The project will adopt a schema design that is versioned, immutable, and normalized. The changes include:
1. **Versioning for Key Attributes**: Attributes such as full name, preferred name, email address, and phone numbers are stored in separate version tables, with timestamps indicating when each version was created.
2. **Phone Number List Entries**: Multiple phone numbers for a customer are supported via a normalized relationship, using a `customer_phone_list_version` table for grouping and a `customer_phone_list_entry` table for individual phone numbers.
3. **Immutable Records**: All version tables are designed to store historical records without modification or deletion.
4. **Validation of Latest Versions**: Triggers ensure that the `customer` table always references the latest version of each attribute.
5. **Materialized Views for Performance**: Materialized views (e.g., `latest_customer_email`) are created to optimize queries for commonly accessed data like the latest email address.

## Consequences

### Benefits
- **Historical Tracking**: Enables full traceability of changes to customer data, enhancing auditability and compliance.
- **Data Integrity**: Triggers and constraints enforce consistent references to the latest versions of data.
- **Improved Flexibility**: Supports complex relationships (e.g., multiple phone numbers) while maintaining clear structure.
- **Immutable Records**: Immutable data ensures that historical changes are preserved without risk of accidental modification.
- **Query Optimization**: Materialized views improve performance for accessing the most recent versions of data.
- **Future-Proof Design**: This schema is extensible, allowing for new attributes or relationships to be added without major redesigns.

### New Endpoint Opportunity
- **Retrieve Customer Profile History**: A single API endpoint will allow retrieval of a customer's full profile history, including changes to their full name, preferred name, email address, and phone numbers. For example:
    - `GET /customers/{customerId}/history`: Returns a comprehensive history of all changes to the customer's profile, grouped by attribute with timestamps for each version.
- **Audit and Traceability**: This endpoint will support compliance requirements and debugging needs by providing a clear, unified view of all changes to a customer’s profile over time.

### Drawbacks
- **Increased Complexity**: The new schema introduces more tables, relationships, and triggers, requiring additional development effort and learning curve.
- **Migration Effort**: Transitioning from the old schema to the new one requires careful planning to avoid data loss or downtime.
- **Write Performance Overhead**: Managing triggers, maintaining immutability, and updating materialized views may introduce minor performance overhead during write operations.
- **API Design Complexity**: While a single endpoint simplifies access to historical data, implementing it may require combining data from multiple version tables, adding some backend complexity.

## Alternatives Considered

- **Maintain the Current Schema**: Rejected due to its inability to support historical tracking, multi-value relationships, and auditability.
- **Use JSON Fields for Versioning**: Rejected as JSON fields complicate querying, indexing, and ensuring data integrity in a relational database.

## Conclusion

By implementing a versioned, immutable, and normalized schema, the project ensures robust historical tracking, improved data integrity, and greater flexibility. A unified API endpoint for retrieving a customer's profile history further enhances the system's value, supporting traceability, auditability, and compliance. Although the approach introduces additional complexity, it aligns with the project’s goals of scalability, auditability, and maintainability.
