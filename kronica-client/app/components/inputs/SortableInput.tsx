"use client";

import {
    DndContext,
    MouseSensor,
    TouchSensor,
    UniqueIdentifier,
    useSensor,
    useSensors,
} from "@dnd-kit/core";
import {
    arrayMove,
    rectSortingStrategy,
    SortableContext,
} from "@dnd-kit/sortable";
import SortableItem from "./Dnd/SortableItem";
import Droppable from "./Dnd/Droppable";
import { Grip } from "lucide-react";

interface InputTypeInterface {
    id: UniqueIdentifier;
}

type ListMutation = {
    id: UniqueIdentifier;
    newIndex: number;
};

interface Props<T> {
    list: T[];
    setList: (arg0: T[]) => void;
    cardComponent: React.ComponentType<{
        item: T;
        list: T[];
        setList: (arg0: T[]) => void;
        index: number;
    }>;
}

export default function SortableInput<T extends InputTypeInterface>({
    list,
    setList,
    cardComponent,
}: Props<T>) {
    const mouseSensor = useSensor(MouseSensor);
    const touchSensor = useSensor(TouchSensor);

    const sensors = useSensors(mouseSensor, touchSensor);

    mouseSensor.sensor.activators = [
        {
            eventName: "onPointerDown",
            handler: ({ nativeEvent: event }) => {
                if (
                    !event.isPrimary ||
                    event.button !== 0 ||
                    isInteractiveElement(event.target)
                ) {
                    return false;
                }

                return true;
            },
        },
    ];

    function isInteractiveElement(element: HTMLElement) {
        const interactiveElements = [
            "button",
            "input",
            "textarea",
            "select",
            "option",
        ];

        if (interactiveElements.includes(element.tagName.toLowerCase())) {
            return true;
        }

        return false;
    }

    function handleDragEnd({ active, over }: any) {
        if (!over) {
            return;
        }

        if (active.id !== over.id) {
            const activeIndex = active.data.current.sortable.index;
            const overIndex = over.data.current?.sortable.index || 0;
            setList(arrayMove(list, activeIndex, overIndex));
        }
    }

    const CardComponent = cardComponent;
    return (
        <DndContext sensors={sensors} onDragEnd={handleDragEnd}>
            <SortableContext items={list} strategy={rectSortingStrategy}>
                <Droppable>
                    {list.map((item, index) => (
                        <div key={item.id} className="my-1">
                            <SortableItem id={item.id}>
                                <div className="content-center p-10">
                                    <Grip />
                                </div>
                                <CardComponent
                                    item={item}
                                    setList={setList}
                                    list={list}
                                    index={index}
                                />
                            </SortableItem>
                        </div>
                    ))}
                </Droppable>
            </SortableContext>
        </DndContext>
    );
}
