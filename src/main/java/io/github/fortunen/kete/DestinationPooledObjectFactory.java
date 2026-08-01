package io.github.fortunen.kete;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import io.github.fortunen.kete.utils.DestinationUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DestinationPooledObjectFactory extends BasePooledObjectFactory<Destination<?>> {

	private DestinationConfig destinationConfig;

	@Override
	public Destination<?> create() {

		Destination<?> destination = null;

		try {

			destination = DestinationUtils.createDestination(destinationConfig);
			destination.initialize();

			return destination;

		} catch (Exception exception) {
			ValidationUtils.tryClose(destination, "destination");
			throw exception;
		}
	}

	@Override
	public PooledObject<Destination<?>> wrap(Destination<?> destination) {
		return new DefaultPooledObject<>(destination);
	}

	@Override
	public boolean validateObject(PooledObject<Destination<?>> pooledObject) {
		try {
			return pooledObject.getObject().isHealthy();
		} catch (Exception exception) {
			return false;
		}
	}

	@Override
	public void destroyObject(PooledObject<Destination<?>> pooledObject) {
		ValidationUtils.tryClose(pooledObject.getObject(), "destination");
	}
}
