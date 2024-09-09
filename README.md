# Satellite Access Tool in Spring

The Satellite Access API provides a simple and powerful way to retrieve satellite access information based on Two-Line Element Sets (TLEs). This API allows you to query access intervals between a specific Earth position and a satellite defined by its TLE, within a given time range and time step.

## Endpoint

```
/api/access/{tle1}/{tle2}/{position}/{startDate}/{endDate}/{timeStep}/{visTH}
```

### Parameters

- `tle1`: Line 1 of the TLE (Two-Line Element Set)
- `tle2`: Line 2 of the TLE
- `position`: Latitude, Longitude, Altitude (degrees, degrees, meters)
- `startDate`: Start date in ISO8601 format
- `endDate`: End date in ISO8601 format
- `timeStep`: Time step in seconds
- `visTH`: Visibility threshold above the horizon in degrees

## Example

Consider the following TLE for the object "FALCON 9 R/B":

```plaintext
FALCON 9 R/B            
1 58348U 23175C   23344.54239545  .00000051  00000+0 -47101-3 0  9991
2 58348   9.0106 159.3778 1972200 262.0639  75.2478  7.34850764  2037
```

For the first hour from January 1, 2024, at 16:00:00, with a threshold of 5 degrees and a time step of 30 seconds, the API request would look like:

```plaintext
/api/access/1%2058348U%2023175C%20%20%2023344.54239545%20%20.00000051%20%2000000+0%20-47101-3%200%20%209991/2%2058348%20%20%209.0106%20159.3778 1972200%20262.0639%20%2075.2478%20%207.34850764%20%202037/0,0,0/2024-01-01T16:00:00.000/2024-01-02T16:00:00.000/30/5
```

This request retrieves access intervals for the specified satellite and Earth position within the given time range and step, considering a visibility threshold above the horizon.