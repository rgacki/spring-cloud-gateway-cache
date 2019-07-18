# Index file layout


| Bytes | Name | Description |
|-------|------|-------------|
| 1 | Valid flag | Determines whether the representation is valid. |
| 1 | Revalidation flag | Must the cache representation be revalidated? |
| 256 | Header hash | The hash created by the response end-to-end headers |
| ? | Revalidation time | The timestamp for when the representation must be revalidated. If 'Revalidation flag' is false, this value is padded |
| ? | Data file name | Name of the data file. |

