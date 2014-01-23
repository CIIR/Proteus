/**
 * Autogenerated by Thrift Compiler (0.9.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package ciir.proteus.thrift;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LongValueList implements org.apache.thrift.TBase<LongValueList, LongValueList._Fields>, java.io.Serializable, Cloneable {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("LongValueList");

  private static final org.apache.thrift.protocol.TField DATES_FIELD_DESC = new org.apache.thrift.protocol.TField("dates", org.apache.thrift.protocol.TType.LIST, (short)1);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new LongValueListStandardSchemeFactory());
    schemes.put(TupleScheme.class, new LongValueListTupleSchemeFactory());
  }

  public List<WeightedDate> dates; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    DATES((short)1, "dates");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // DATES
          return DATES;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.DATES, new org.apache.thrift.meta_data.FieldMetaData("dates", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, WeightedDate.class))));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(LongValueList.class, metaDataMap);
  }

  public LongValueList() {
  }

  public LongValueList(
    List<WeightedDate> dates)
  {
    this();
    this.dates = dates;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public LongValueList(LongValueList other) {
    if (other.isSetDates()) {
      List<WeightedDate> __this__dates = new ArrayList<WeightedDate>();
      for (WeightedDate other_element : other.dates) {
        __this__dates.add(new WeightedDate(other_element));
      }
      this.dates = __this__dates;
    }
  }

  public LongValueList deepCopy() {
    return new LongValueList(this);
  }

  @Override
  public void clear() {
    this.dates = null;
  }

  public int getDatesSize() {
    return (this.dates == null) ? 0 : this.dates.size();
  }

  public java.util.Iterator<WeightedDate> getDatesIterator() {
    return (this.dates == null) ? null : this.dates.iterator();
  }

  public void addToDates(WeightedDate elem) {
    if (this.dates == null) {
      this.dates = new ArrayList<WeightedDate>();
    }
    this.dates.add(elem);
  }

  public List<WeightedDate> getDates() {
    return this.dates;
  }

  public LongValueList setDates(List<WeightedDate> dates) {
    this.dates = dates;
    return this;
  }

  public void unsetDates() {
    this.dates = null;
  }

  /** Returns true if field dates is set (has been assigned a value) and false otherwise */
  public boolean isSetDates() {
    return this.dates != null;
  }

  public void setDatesIsSet(boolean value) {
    if (!value) {
      this.dates = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case DATES:
      if (value == null) {
        unsetDates();
      } else {
        setDates((List<WeightedDate>)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case DATES:
      return getDates();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case DATES:
      return isSetDates();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof LongValueList)
      return this.equals((LongValueList)that);
    return false;
  }

  public boolean equals(LongValueList that) {
    if (that == null)
      return false;

    boolean this_present_dates = true && this.isSetDates();
    boolean that_present_dates = true && that.isSetDates();
    if (this_present_dates || that_present_dates) {
      if (!(this_present_dates && that_present_dates))
        return false;
      if (!this.dates.equals(that.dates))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    HashCodeBuilder builder = new HashCodeBuilder();

    boolean present_dates = true && (isSetDates());
    builder.append(present_dates);
    if (present_dates)
      builder.append(dates);

    return builder.toHashCode();
  }

  public int compareTo(LongValueList other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    LongValueList typedOther = (LongValueList)other;

    lastComparison = Boolean.valueOf(isSetDates()).compareTo(typedOther.isSetDates());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetDates()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.dates, typedOther.dates);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("LongValueList(");
    boolean first = true;

    sb.append("dates:");
    if (this.dates == null) {
      sb.append("null");
    } else {
      sb.append(this.dates);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class LongValueListStandardSchemeFactory implements SchemeFactory {
    public LongValueListStandardScheme getScheme() {
      return new LongValueListStandardScheme();
    }
  }

  private static class LongValueListStandardScheme extends StandardScheme<LongValueList> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, LongValueList struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // DATES
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list42 = iprot.readListBegin();
                struct.dates = new ArrayList<WeightedDate>(_list42.size);
                for (int _i43 = 0; _i43 < _list42.size; ++_i43)
                {
                  WeightedDate _elem44; // required
                  _elem44 = new WeightedDate();
                  _elem44.read(iprot);
                  struct.dates.add(_elem44);
                }
                iprot.readListEnd();
              }
              struct.setDatesIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, LongValueList struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.dates != null) {
        oprot.writeFieldBegin(DATES_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.dates.size()));
          for (WeightedDate _iter45 : struct.dates)
          {
            _iter45.write(oprot);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class LongValueListTupleSchemeFactory implements SchemeFactory {
    public LongValueListTupleScheme getScheme() {
      return new LongValueListTupleScheme();
    }
  }

  private static class LongValueListTupleScheme extends TupleScheme<LongValueList> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, LongValueList struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetDates()) {
        optionals.set(0);
      }
      oprot.writeBitSet(optionals, 1);
      if (struct.isSetDates()) {
        {
          oprot.writeI32(struct.dates.size());
          for (WeightedDate _iter46 : struct.dates)
          {
            _iter46.write(oprot);
          }
        }
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, LongValueList struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(1);
      if (incoming.get(0)) {
        {
          org.apache.thrift.protocol.TList _list47 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
          struct.dates = new ArrayList<WeightedDate>(_list47.size);
          for (int _i48 = 0; _i48 < _list47.size; ++_i48)
          {
            WeightedDate _elem49; // required
            _elem49 = new WeightedDate();
            _elem49.read(iprot);
            struct.dates.add(_elem49);
          }
        }
        struct.setDatesIsSet(true);
      }
    }
  }

}

