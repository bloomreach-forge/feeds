# Bloomreach Feeds Plugin - User Guide Folder

## Start Here

**[USER_GUIDE.md](USER_GUIDE.md)** - Complete, streamlined user guide covering all features and usage patterns.

This is your primary reference for:
- Property-based feed filtering
- Atom feed enrichment
- Configuration and setup
- Testing and validation
- Troubleshooting

## Documentation Organization

The root directory contains legacy documentation files that have been consolidated here:

| Legacy File | Status | Replacement |
|-------------|--------|-------------|
| `../PROPERTY_FILTERING_GUIDE.md` | **Kept for reference** | USER_GUIDE.md § Property-Based Feed Filtering |
| `../ATOM_ENRICHMENT_GUIDE.md` | **Kept for reference** | USER_GUIDE.md § Atom Feed Enrichment |
| `../WILDCARD_FILTERING_CHANGELOG.md` | **Obsolete** | USER_GUIDE.md § Filter Syntax |
| `../FILTER_SYNTAX_QUICK_REFERENCE.md` | **Obsolete** | USER_GUIDE.md § Filter Syntax |
| `../IMPLEMENTATION_SUMMARY.md` | **Obsolete** | Technical implementation (not user-facing) |
| `../ATOM_ENRICHMENT_IMPLEMENTATION.md` | **Obsolete** | Technical implementation (not user-facing) |
| `../FORGE-325-FIX-SUMMARY.md` | **Obsolete** | Technical implementation (not user-facing) |
| `../LOCAL_TESTING_INSTRUCTIONS.md` | **Kept for reference** | USER_GUIDE.md § Testing & Validation |
| `../TESTING_GUIDE.md` | **Kept for reference** | USER_GUIDE.md § Testing & Validation |

## Quick Navigation

### For End Users
- **Getting Started**: USER_GUIDE.md → Property-Based Feed Filtering → Configuration
- **Filter Examples**: USER_GUIDE.md → Property-Based Feed Filtering → Practical Examples
- **Troubleshooting**: USER_GUIDE.md → Troubleshooting

### For Developers
- **Testing**: USER_GUIDE.md → Testing & Validation
- **Custom Implementations**: USER_GUIDE.md → Property-Based Feed Filtering → Advanced Usage
- **Enrichment Extension**: USER_GUIDE.md → Atom Feed Enrichment (combine with filtering)

## What Changed

✅ **Consolidated** all redundant documentation into a single, organized guide
✅ **Removed** multiple implementation summaries and changelogs
✅ **Organized** by feature and use case for easy navigation
✅ **Kept** reference documentation for detailed deep-dives

## Reference Documentation

The following legacy files are retained for additional technical context:

- `../PROPERTY_FILTERING_GUIDE.md` - Detailed property filtering reference
- `../ATOM_ENRICHMENT_GUIDE.md` - Detailed atom enrichment reference
- `../LOCAL_TESTING_INSTRUCTIONS.md` - Step-by-step testing procedures
- `../TESTING_GUIDE.md` - Testing framework details

These can be consulted for implementation-specific details not covered in the main user guide.

## File Structure

```
feeds/
├── userguide/
│   ├── README.md (this file)
│   └── USER_GUIDE.md (start here)
├── README.md (project overview)
├── PROPERTY_FILTERING_GUIDE.md (reference)
├── ATOM_ENRICHMENT_GUIDE.md (reference)
├── LOCAL_TESTING_INSTRUCTIONS.md (reference)
└── TESTING_GUIDE.md (reference)
```

## Updates & Maintenance

When documentation changes:
1. Update `USER_GUIDE.md` with user-facing changes
2. Update reference docs only if technical implementation changes
3. Obsolete files can be safely removed after confirming consolidation is complete
