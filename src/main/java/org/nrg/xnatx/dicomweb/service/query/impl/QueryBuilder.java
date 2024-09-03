/********************************************************************
 * Copyright (c) 2023, Institute of Cancer Research
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * (1) Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 * (2) Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution.
 *
 * (3) Neither the name of the Institute of Cancer Research nor the
 *     names of its contributors may be used to endorse or promote
 *     products derived from this software without specific prior
 *     written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *********************************************************************/
package org.nrg.xnatx.dicomweb.service.query.impl;

import org.dcm4che3.data.*;
import org.dcm4che3.util.DateUtils;
import org.hibernate.criterion.*;
import org.nrg.xnatx.dicomweb.conf.privateelements.PrivateTag;
import org.nrg.xnatx.dicomweb.entity.DwSeries;
import org.nrg.xnatx.dicomweb.conf.QueryRetrieveLevel2;

import java.util.*;

/**
 * @author mo.alsad
 *
 * Some parts of code are refactored from dcm4chee. Refer to DCM4CHEE_LICENSE.
 */
public class QueryBuilder
{
	public static void addPredicatesToCriteria(DetachedCriteria criteria,
		List<Criterion> predicates)
	{
		for (Criterion c : predicates)
		{
			criteria.add(c);
		}
	}

	public static boolean hasPatientLevelCriteria(IDWithIssuer[] pids,
		Attributes keys)
	{
		if (!isUniversalMatching(pids))
		{
			return true;
		}

		return !isUniversalMatching(keys.getString(Tag.PatientName))
						 || !isUniversalMatching(keys.getString(Tag.PatientSex))
						 || !isUniversalMatching(keys.getString(Tag.PatientSex))
						 || !isUniversalMatching(keys.getString(Tag.PatientBirthDate));
	}

	public static void instancePredicates(DetachedCriteria instance,
		IDWithIssuer[] pids, Attributes keys)
	{
		DetachedCriteria series = instance.createAlias("instance.series", "series");
		DetachedCriteria study = series.createAlias("series.study", "study");
		DetachedCriteria patient = study.createAlias("study.patient", "patient");

		patientLevelPredicates(patient, pids, keys, QueryRetrieveLevel2.IMAGE);
		studyLevelPredicates(study, keys, QueryRetrieveLevel2.IMAGE);
		seriesLevelPredicates(series, keys, QueryRetrieveLevel2.SERIES);
		instanceLevelPredicates(instance, keys);
	}

	public static boolean isUniversalMatching(String[] values)
	{
		return values == null || values.length == 0 || values[0] == null || values[0].equals(
			"*");
	}

	public static boolean isUniversalMatching(IDWithIssuer[] pids)
	{
		for (IDWithIssuer pid : pids)
		{
			if (!isUniversalMatching(pid.getID()))
			{
				return false;
			}
		}
		return true;
	}

	public static void orderInstances(DetachedCriteria instance,
		List<OrderByTag> orderByTags)
	{
		List<Order> orderList = new ArrayList<>(orderByTags.size());
		for (OrderByTag orderByTag : orderByTags)
		{
			orderInstances(orderByTag, orderList);
		}
		for (Order order : orderList)
		{
			instance.addOrder(order);
		}
	}

	public static void orderSeries(DetachedCriteria series,
		List<OrderByTag> orderByTags)
	{
		List<Order> orderList = new ArrayList<>(orderByTags.size());
		for (OrderByTag orderByTag : orderByTags)
		{
			orderSeries(orderByTag, orderList);
		}
		for (Order order : orderList)
		{
			series.addOrder(order);
		}
	}

	public static void orderStudies(DetachedCriteria study,
		List<OrderByTag> orderByTags)
	{
		List<Order> orderList = new ArrayList<>(orderByTags.size());
		for (OrderByTag orderByTag : orderByTags)
		{
			orderStudies(orderByTag, orderList);
		}
		for (Order order : orderList)
		{
			study.addOrder(order);
		}
	}

	public static void seriesPredicates(DetachedCriteria series,
		IDWithIssuer[] pids, Attributes keys)
	{
		DetachedCriteria study = series.createAlias("series.study", "study");
		DetachedCriteria patient = study.createAlias("study.patient", "patient");

		patientLevelPredicates(patient, pids, keys, QueryRetrieveLevel2.SERIES);
		studyLevelPredicates(study, keys, QueryRetrieveLevel2.SERIES);
		seriesLevelPredicates(series, keys, QueryRetrieveLevel2.SERIES);
	}

	public static void studyPredicates(DetachedCriteria study,
		IDWithIssuer[] pids, Attributes keys, boolean queryPatient)
	{
		if (queryPatient)
		{
			DetachedCriteria patient = study.createAlias("study.patient", "patient");
			patientLevelPredicates(patient, pids, keys, QueryRetrieveLevel2.STUDY);
		}
		studyLevelPredicates(study, keys, QueryRetrieveLevel2.STUDY);
	}

	public static void uidsPredicate(List<Criterion> predicates, String property,
		String value)
	{
		if (!isUniversalMatching(value))
		{
			predicates.add(Restrictions.eq(property, value));
		}
	}

	public static long unbox(Long value, long defaultValue)
	{
		return value != null ? value.longValue() : defaultValue;
	}

	private static boolean anyOf(List<Criterion> predicates, String property,
		String[] values, boolean ignoreCase)
	{
		if (isUniversalMatching(values))
		{
			return false;
		}

		if (values.length == 1)
		{
			return wildCard(predicates, property, values[0], ignoreCase);
		}

		List<Criterion> predicatesI = new ArrayList<>(values.length);
		for (String value : values)
		{
			if (!wildCard(predicatesI, property, value, ignoreCase))
			{
				return false;
			}
		}
		predicates.add(Restrictions.or(predicatesI.toArray(new Criterion[0])));

		return true;
	}

	private static Criterion combinedRange(String dateProperty,
		String timeProperty, DateRange dateRange)
	{
		if (dateRange.getStartDate() == null)
		{
			return combinedRangeEnd(dateProperty, timeProperty,
				DateUtils.formatDA(null, dateRange.getEndDate()),
				DateUtils.formatTM(null, dateRange.getEndDate()));
		}
		if (dateRange.getEndDate() == null)
		{
			return combinedRangeStart(dateProperty, timeProperty,
				DateUtils.formatDA(null, dateRange.getStartDate()),
				DateUtils.formatTM(null, dateRange.getStartDate()));
		}
		return combinedRangeInterval(dateProperty, timeProperty,
			dateRange.getStartDate(), dateRange.getEndDate());
	}

	private static Criterion combinedRangeEnd(String dateProperty,
		String timeProperty, String endDate, String endTime)
	{
		return Restrictions.or(
			Restrictions.lt(dateProperty, endDate),
			Restrictions.and(
				Restrictions.eq(dateProperty, endDate),
				Restrictions.or(
					Restrictions.le(timeProperty, endTime),
					Restrictions.eq(timeProperty, "*"))));
	}

	private static Criterion combinedRangeInterval(String dateProperty,
		String timeProperty, Date startDateRange, Date endDateRange)
	{
		String startTime = DateUtils.formatTM(null, startDateRange);
		String endTime = DateUtils.formatTM(null, endDateRange);
		String startDate = DateUtils.formatDA(null, startDateRange);
		String endDate = DateUtils.formatDA(null, endDateRange);
		return endDate.equals(startDate)
						 ? Restrictions.and(
			Restrictions.eq(dateProperty, startDate),
			Restrictions.ge(timeProperty, startTime),
			Restrictions.le(timeProperty, endTime))
						 : Restrictions.and(
			combinedRangeStart(dateProperty, timeProperty, startDate,
				startTime),
			combinedRangeEnd(dateProperty, timeProperty, endDate, endTime));
	}

	private static Criterion combinedRangeStart(String dateProperty,
		String timeProperty, String startDate, String startTime)
	{
		return Restrictions.or(
			Restrictions.gt(dateProperty, startDate),
			Restrictions.and(
				Restrictions.eq(dateProperty, startDate),
				Restrictions.or(
					Restrictions.ge(timeProperty, startTime),
					Restrictions.eq(timeProperty, "*"))));
	}

	private static boolean containsWildcard(String s)
	{
		return s.indexOf('*') >= 0 || s.indexOf('?') >= 0;
	}

	private static Criterion dateRange(String property, DateRange range,
		FormatDate dt)
	{
		String start = format(range.getStartDate(), dt);
		String end = format(range.getEndDate(), dt);
		return start == null ? Restrictions.le(property, end)
						 : end == null ? Restrictions.ge(property, start)
								 : start.equals(end) ? Restrictions.eq(property, start)
										 : (dt.equals(
			FormatDate.TM) && range.isStartDateExeedsEndDate())
												 ? Restrictions.or(
			Restrictions.between(property, start, "115959.999"),
			Restrictions.between(property, "000000.000", end))
												 : Restrictions.between(property, start, end);
	}

	private static void dateRange(List<Criterion> predicates, String property,
		DateRange range, FormatDate dt)
	{
		if (!isUniversalMatching(range))
		{
			predicates.add(dateRange(property, range, dt));
			predicates.add(Restrictions.ne(property, "*"));
		}
	}

	private static Criterion dateRange(String property, DateRange range)
	{
		Date start = range.getStartDate();
		Date end = range.getEndDate();
		return start == null ? Restrictions.le(property, end)
						 : end == null ? Restrictions.ge(property, start)
								 : start.equals(end) ? Restrictions.eq(property, start)
										 : Restrictions.between(property, start, end);
	}

	private static void dateRange(List<Criterion> predicates, String dateProperty,
		String timeProperty, int dateTag, int timeTag, long dateAndTimeTag,
		Attributes keys)
	{
		// ToDo: make combinedDatetimeMatching optional
		boolean combinedDatetimeMatching = true;
		DateRange dateRange = keys.getDateRange(dateTag, null);
		DateRange timeRange = keys.getDateRange(timeTag, null);
		if (combinedDatetimeMatching && !isUniversalMatching(
			dateRange) && !isUniversalMatching(timeRange))
		{
			predicates.add(combinedRange(dateProperty, timeProperty,
				keys.getDateRange(dateAndTimeTag, null)));
			predicates.add(Restrictions.ne(dateProperty, "*"));
		}
		else
		{
			dateRange(predicates, dateProperty, dateRange, FormatDate.DA);
			dateRange(predicates, dateProperty, timeRange, FormatDate.TM);
		}
	}

	private static String format(Date date, FormatDate dt)
	{
		return date != null ? dt.format(date) : null;
	}

	private static void instanceLevelPredicates(DetachedCriteria instance,
		Attributes keys)
	{
		List<Criterion> predicates = new ArrayList<>();

		anyOf(predicates, "instance.sopInstanceUid",
			keys.getStrings(Tag.SOPInstanceUID), false);
		anyOf(predicates, "instance.sopClassUid",
			keys.getStrings(Tag.SOPClassUID), false);
		numberPredicate(predicates, "instance.instanceNumber",
			keys.getString(Tag.InstanceNumber, "*"));
		dateRange(predicates,
			"instance.contentDate",
			"instance.contentTime",
			Tag.ContentDate, Tag.ContentTime, Tag.ContentDateAndTime, keys);

		addPredicatesToCriteria(instance, predicates);
	}

	private static boolean isUniversalMatching(Attributes item)
	{
		return item == null || item.isEmpty();
	}

	private static boolean isUniversalMatching(String value)
	{
		return value == null || value.equals("*");
	}

	private static boolean isUniversalMatching(DateRange range)
	{
		return range == null || (range.getStartDate() == null && range.getEndDate() == null);
	}

	private static void numberPredicate(List<Criterion> predicates,
		String property,
		String value)
	{
		if (!isUniversalMatching(value))
		{
			try
			{
				predicates.add(Restrictions.eq(property, Integer.parseInt(value)));
			}
			catch (NumberFormatException ignored)
			{
			}
		}
	}

	private static boolean orderInstances(OrderByTag orderByTag,
		List<Order> orderList)
	{
		if (orderSeries(orderByTag, orderList))
		{
			return true;
		}

		switch (orderByTag.tag)
		{
			case Tag.SOPInstanceUID:
				return orderList.add(orderByTag.order("instance.sopInstanceUid"));
			case Tag.SOPClassUID:
				return orderList.add(orderByTag.order("instance.sopClassUid"));
			case Tag.InstanceNumber:
				return orderList.add(orderByTag.order("instance.instanceNumber"));
			case Tag.ContentDate:
				return orderList.add(orderByTag.order("instance.contentDate"));
			case Tag.ContentTime:
				return orderList.add(orderByTag.order("instance.contentTime"));
		}
		return false;
	}

	private static boolean orderPatients(OrderByTag orderByTag,
		List<Order> orderList)
	{
		switch (orderByTag.tag)
		{
			case Tag.PatientName:
				return orderList.add(orderByTag.order("patient.patientName"));
			case Tag.PatientSex:
				return orderList.add(orderByTag.order("patient.patientSex"));
			case Tag.PatientBirthDate:
				return orderList.add(
					orderByTag.order("patient.patientBirthDate"));
		}
		return false;
	}

	private static boolean orderSeries(OrderByTag orderByTag,
		List<Order> orderList)
	{
		if (orderStudies(orderByTag, orderList))
		{
			return true;
		}

		switch (orderByTag.tag)
		{
			case Tag.SeriesInstanceUID:
				return orderList.add(
					orderByTag.order("series.seriesInstanceUid"));
			case Tag.SeriesNumber:
				return orderList.add(orderByTag.order("series.seriesNumber"));
			case Tag.Modality:
				return orderList.add(orderByTag.order("series.modality"));
			case Tag.BodyPartExamined:
				return orderList.add(
					orderByTag.order("series.bodyPartExamined"));
			case Tag.Laterality:
				return orderList.add(orderByTag.order("series.laterality"));
			case Tag.PerformedProcedureStepStartDate:
				return orderList.add(
					orderByTag.order("series.performedProcedureStepStartDate"));
			case Tag.PerformedProcedureStepStartTime:
				return orderList.add(
					orderByTag.order("series.performedProcedureStepStartTime"));
			case Tag.SeriesDescription:
				return orderList.add(orderByTag.order("series.seriesDescription"));
			case Tag.StationName:
				return orderList.add(orderByTag.order("series.stationName"));
			case Tag.InstitutionName:
				return orderList.add(orderByTag.order("series.institutionName"));
			case Tag.InstitutionalDepartmentName:
				return orderList.add(
					orderByTag.order("series.institutionalDepartmentName"));
		}
		return false;
	}

	private static boolean orderStudies(OrderByTag orderByTag,
		List<Order> orderList)
	{
		if (orderPatients(orderByTag, orderList))
		{
			return true;
		}

		switch (orderByTag.tag)
		{
			case Tag.StudyInstanceUID:
				return orderList.add(orderByTag.order("study.studyInstanceUid"));
			case Tag.StudyID:
				return orderList.add(orderByTag.order("study.studyId"));
			case Tag.StudyDate:
				return orderList.add(orderByTag.order("study.studyDate"));
			case Tag.StudyTime:
				return orderList.add(orderByTag.order("study.studyTime"));
			case Tag.StudyDescription:
				return orderList.add(orderByTag.order("study.studyDescription"));
			case Tag.AccessionNumber:
				return orderList.add(orderByTag.order("study.accessionNumber"));
		}
		return false;
	}

	private static DateRange parseDateRange(String s)
	{
		if (s == null)
		{
			return null;
		}

		String[] range = splitRange(s);
		DatePrecision precision = new DatePrecision();
		Date start = range[0] == null ? null
									 : VR.DT.toDate(range[0], null, 0, false, null, precision);
		Date end = range[1] == null ? null
								 : VR.DT.toDate(range[1], null, 0, true, null, precision);
		return new DateRange(start, end);
	}

	private static void patientLevelPredicates(DetachedCriteria patient,
		IDWithIssuer[] pids, Attributes keys,
		QueryRetrieveLevel2 queryRetrieveLevel)
	{
		List<Criterion> predicates = new ArrayList<>();

		if (queryRetrieveLevel == QueryRetrieveLevel2.PATIENT)
		{
			// Todo: make onlyWithStudies optional
			predicates.add(Restrictions.gt("patient.numberOfStudies",
				0));
		}
		String[] patientIds = Arrays.stream(pids).map(IDWithIssuer::getID)
																.toArray(size -> {
																	return new String[pids.length];
																});
		anyOf(predicates, "patient.patientId", patientIds, true);
		anyOf(predicates, "patient.patientName",
			keys.getStrings(Tag.PatientName), true);
		anyOf(predicates, "patient.patientSex",
			toUpperCase(keys.getStrings(Tag.PatientSex)), false);
		dateRange(predicates, "patient.patientBirthDate",
			keys.getDateRange(Tag.PatientBirthDate), FormatDate.DA);

		addPredicatesToCriteria(patient, predicates);
	}

	private static void seriesAttributesInStudy(List<Criterion> studyPredicates,
		Attributes keys, QueryRetrieveLevel2 queryRetrieveLevel,
		String... modalitiesInStudy)
	{
		List<Criterion> predicates = new ArrayList<>();

		anyOf(predicates, "series.modality", toUpperCase(modalitiesInStudy), false);
		String[] cuidsInStudy = keys.getStrings(Tag.SOPClassesInStudy);
		if (!isUniversalMatching(cuidsInStudy))
		{
			predicates.add(Restrictions.in("series.sopClassUid",
				cuidsInStudy));
		}
		if (queryRetrieveLevel == QueryRetrieveLevel2.STUDY)
		{
			anyOf(predicates, "series.institutionName",
				keys.getStrings(Tag.InstitutionName), true);
			anyOf(predicates, "series.institutionalDepartmentName",
				keys.getStrings(Tag.InstitutionalDepartmentName), true);
			anyOf(predicates, "series.stationName",
				keys.getStrings(Tag.StationName), true);
			anyOf(predicates, "series.seriesDescription",
				keys.getStrings(Tag.SeriesDescription), true);
			anyOf(predicates, "series.bodyPartExamined",
				toUpperCase(keys.getStrings(Tag.BodyPartExamined)), false);
			anyOf(predicates, "series.laterality",
				toUpperCase(keys.getStrings(Tag.Laterality)), false);
		}
		if (!predicates.isEmpty())
		{
			DetachedCriteria series = DetachedCriteria.forClass(DwSeries.class,
				"series");
			// Projection is unnecessary - used here due to a bug in Hibernate
			series.setProjection(Property.forName("series.study"));
			series.add(Restrictions.eqProperty("series.study", "study"));
			addPredicatesToCriteria(series, predicates);
			studyPredicates.add(Subqueries.exists(series));
		}
	}

	private static void seriesLevelPredicates(DetachedCriteria series,
		Attributes keys, QueryRetrieveLevel2 queryRetrieveLevel)
	{
		List<Criterion> predicates = new ArrayList<>();

		anyOf(predicates, "series.seriesInstanceUid",
			keys.getStrings(Tag.SeriesInstanceUID), false);
		numberPredicate(predicates, "series.seriesNumber",
			keys.getString(Tag.SeriesNumber, "*"));
		anyOf(predicates, "series.modality",
			toUpperCase(keys.getStrings(Tag.Modality)), false);
		anyOf(predicates, "series.bodyPartExamined",
			toUpperCase(keys.getStrings(Tag.BodyPartExamined)), false);
		anyOf(predicates, "series.laterality",
			toUpperCase(keys.getStrings(Tag.Laterality)), false);
		dateRange(predicates,
			"series.performedProcedureStepStartDate",
			"series.performedProcedureStepStartTime",
			Tag.PerformedProcedureStepStartDate, Tag.PerformedProcedureStepStartTime,
			Tag.PerformedProcedureStepStartDateAndTime, keys);
		anyOf(predicates, "series.seriesDescription",
			keys.getStrings(Tag.SeriesDescription), true);
		anyOf(predicates, "series.stationName",
			keys.getStrings(Tag.StationName), true);
		anyOf(predicates, "series.institutionalDepartmentName",
			keys.getStrings(Tag.InstitutionalDepartmentName), true);
		anyOf(predicates, "series.institutionName",
			keys.getStrings(Tag.InstitutionName), true);

		addPredicatesToCriteria(series, predicates);
	}

	private static String[] splitRange(String s)
	{
		String[] range = new String[2];
		int delim = s.indexOf('-');
		if (delim == -1)
		{
			range[0] = range[1] = s;
		}
		else
		{
			if (delim > 0)
			{
				range[0] = s.substring(0, delim);
			}
			if (delim < s.length() - 1)
			{
				range[1] = s.substring(delim + 1);
			}
		}
		return range;
	}

	private static void studyLevelPredicates(DetachedCriteria study,
		Attributes keys, QueryRetrieveLevel2 queryRetrieveLevel)
	{
		List<Criterion> predicates = new ArrayList<>();

		predicates.add(Restrictions.eq("study.sessionId",
			keys.getString(PrivateTag.PrivateCreator, PrivateTag.XNATExperimentID,
				VR.LO)));

		anyOf(predicates, "study.studyInstanceUid",
			keys.getStrings(Tag.StudyInstanceUID), false);
		anyOf(predicates, "study.studyId", keys.getStrings(Tag.StudyID),
			false);
		dateRange(predicates, "study.studyDate", "study.studyTime",
			Tag.StudyDate, Tag.StudyTime, Tag.StudyDateAndTime, keys);
		anyOf(predicates, "study.studyDescription",
			keys.getStrings(Tag.StudyDescription), true);

		String[] accNo = new String[]{
			keys.getString(Tag.AccessionNumber, "*")};
		anyOf(predicates, "study.accessionNumber", accNo, false);

		String[] modalitiesInStudy = keys.getStrings(Tag.ModalitiesInStudy);
		seriesAttributesInStudy(predicates, keys, queryRetrieveLevel,
			modalitiesInStudy);

		addPredicatesToCriteria(study, predicates);
	}

	private static String toLikePattern(String s)
	{
		StringBuilder like = new StringBuilder(s.length());
		char[] cs = s.toCharArray();
		char p = 0;
		for (char c : cs)
		{
			switch (c)
			{
				case '*':
					if (c != p)
					{
						like.append('%');
					}
					break;
				case '?':
					like.append('_');
					break;
				case '_':
				case '%':
				case '!':
					like.append('!');
					// fall through
				default:
					like.append(c);
			}
			p = c;
		}
		return like.toString();
	}

	private static String[] toUpperCase(String[] ss)
	{
		if (ss != null)
		{
			for (int i = 0; i < ss.length; i++)
				ss[i] = ss[i].toUpperCase();
		}
		return ss;
	}

	private static boolean wildCard(List<Criterion> predicates, String property,
		String value)
	{
		return wildCard(predicates, property, value, false);
	}

	private static boolean wildCard(List<Criterion> predicates,
		String property, String value, boolean ignoreCase)
	{
		if (isUniversalMatching(value))
		{
			return false;
		}

		if (containsWildcard(value))
		{
			String pattern = toLikePattern(value);
			if (pattern.equals("%"))
			{
				return false;
			}
			predicates.add(new IcrLikeExpression(property, value, MatchMode.ANYWHERE,
				'!', ignoreCase));
		}
		else
		{
			predicates.add(Restrictions.eq(property, value));
		}

		return true;
	}

	private enum FormatDate
	{
		DA
			{
				@Override
				String format(Date date)
				{
					return DateUtils.formatDA(null, date);
				}
			},
		TM
			{
				@Override
				String format(Date date)
				{
					return DateUtils.formatTM(null, date);
				}
			},
		DT
			{
				@Override
				String format(Date date)
				{
					return DateUtils.formatDT(null, date);
				}
			};

		abstract String format(Date date);
	}
}
